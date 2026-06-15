#!/usr/bin/env bash
# Builds mjdev-desktop-<version>.iso: a minimal, bootable Debian (latest stable)
# live image carrying only wayland + the mjdev desktop — no X server, no desktop
# environment, no recommends. It enables the non-free driver/firmware set, installs
# every *.deb from deb-packages/ (plymouth + cursor + sound themes) and activates
# them, and autostarts the mjdev wayland session on boot.
#
# Name + version come from the gradle version catalog (never hardcoded). The
# desktop app deb and the compositor/session files must already be built — the
# gradle `makeIso` task wires those dependencies and passes their paths in.
# This script only assembles the image and must run as root (debootstrap +
# chroot + mksquashfs).
#
#   sudo ./make-iso.sh                     # uses the built deb + compositor + deb-packages/
#   sudo ./make-iso.sh --suite trixie      # pin a specific debian suite
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
CATALOG="$HERE/gradle/libs.versions.toml"

read_catalog() { sed -n "s/^$1 *= *\"\(.*\)\"/\1/p" "$CATALOG"; }
APP_NAME="$(read_catalog app-name)"
VERSION="$(read_catalog app-pkg-version)"
[ -n "$APP_NAME" ] && [ -n "$VERSION" ] || { echo "cannot read app name/version from $CATALOG"; exit 1; }
# wayland runtime stack for mjdevc — single source of truth in the version catalog (not hardcoded)
RUNTIME_DEPS="$(read_catalog app-compositor-runtime-deps)"

# ---- config / args -------------------------------------------------------
# trixie = Debian 13, the current stable ("latest public version"). A bare
# "stable" can symlink to sid on non-debian hosts, so a concrete codename is used.
SUITE="${MJDEV_ISO_SUITE:-trixie}"
MIRROR="${MJDEV_ISO_MIRROR:-http://deb.debian.org/debian}"
# building a debian rootfs from a non-debian host needs the debian archive key
KEYRING="/usr/share/keyrings/debian-archive-keyring.gpg"
# the gradle task overrides these; the defaults match the in-tree build outputs
DEB="${MJDEV_ISO_DEB:-$(ls "$HERE"/packages/main-release/deb/*.deb 2>/dev/null | head -n1 || true)}"
COMPOSITOR_BIN="${MJDEV_ISO_COMPOSITOR_BIN:-$HERE/compositor/build/session-install/mjdevc}"
SESSION_DIR="${MJDEV_ISO_SESSION_DIR:-$HERE/compositor/build/session-install}"
EXTRA_DEBS_DIR="${MJDEV_ISO_EXTRA_DEBS:-$HERE/deb-packages}"
OUT="${MJDEV_ISO_OUT:-$HERE/releases/$APP_NAME-$VERSION.iso}"
LIVE_USER="mjdev"
WORK=""

while [ $# -gt 0 ]; do
    case "$1" in
        --deb) DEB="$2"; shift 2;;
        --compositor-bin) COMPOSITOR_BIN="$2"; shift 2;;
        --session-dir) SESSION_DIR="$2"; shift 2;;
        --extra-debs) EXTRA_DEBS_DIR="$2"; shift 2;;
        --out) OUT="$2"; shift 2;;
        --suite) SUITE="$2"; shift 2;;
        --mirror) MIRROR="$2"; shift 2;;
        --work) WORK="$2"; shift 2;;
        -h|--help) sed -n '2,16p' "$0"; exit 0;;
        *) echo "unknown arg: $1"; exit 1;;
    esac
done

# tee everything to a build log so failures are diagnosable even when the script
# is run through pkexec (which detaches the inherited stdio from the caller).
LOG="${MJDEV_ISO_LOG:-/tmp/mjdev-iso-build.log}"
exec > >(tee "$LOG") 2>&1
echo ">> mjdev iso build log -> $LOG"

[ "$(id -u)" -eq 0 ] || { echo "must run as root (debootstrap/chroot)"; exit 1; }
[ -n "$DEB" ] && [ -f "$DEB" ] || { echo "desktop deb not found: '${DEB:-}'"; echo "build it first: ./gradlew :desktopApp:packageReleaseDeb"; exit 1; }
[ -f "$COMPOSITOR_BIN" ] || { echo "compositor binary not found: $COMPOSITOR_BIN"; echo "build it first: ./gradlew :compositor:stageSession"; exit 1; }
for t in debootstrap mksquashfs xorriso grub-mkrescue; do
    command -v "$t" >/dev/null 2>&1 || { echo "missing tool: $t (apt install debootstrap squashfs-tools xorriso grub-common grub-pc-bin grub-efi-amd64-bin)"; exit 1; }
done

DEB="$(readlink -f "$DEB")"
COMPOSITOR_BIN="$(readlink -f "$COMPOSITOR_BIN")"
SESSION_DIR="$(readlink -f "$SESSION_DIR")"
mkdir -p "$(dirname "$OUT")"; OUT="$(readlink -f "$OUT")"
[ -d "$EXTRA_DEBS_DIR" ] && EXTRA_DEBS_DIR="$(readlink -f "$EXTRA_DEBS_DIR")" || EXTRA_DEBS_DIR=""
WORK="${WORK:-$(mktemp -d /tmp/mjdev-iso.XXXXXX)}"
ROOT="$WORK/rootfs"; ISO="$WORK/iso"
mkdir -p "$ROOT" "$ISO/live" "$ISO/boot/grub"

cleanup() { umount -lf "$ROOT/dev/pts" "$ROOT/dev" "$ROOT/proc" "$ROOT/sys" 2>/dev/null || true; }
trap cleanup EXIT

# ---- 1. minimal base -----------------------------------------------------
echo ">> debootstrap $SUITE (minbase) -> $ROOT"
DEBOOTSTRAP_OPTS="--variant=minbase"
[ -f "$KEYRING" ] && DEBOOTSTRAP_OPTS="$DEBOOTSTRAP_OPTS --keyring=$KEYRING"
# shellcheck disable=SC2086
debootstrap $DEBOOTSTRAP_OPTS "$SUITE" "$ROOT" "$MIRROR"

mount --bind /dev "$ROOT/dev"
mount -t devpts devpts "$ROOT/dev/pts" 2>/dev/null || true
mount -t proc proc "$ROOT/proc"
mount -t sysfs sys "$ROOT/sys"

# the fresh chroot has no DNS resolver — copy the host's so apt-get inside the
# chroot can reach the mirror (without this apt-get update fails -> exit 100).
cp -L /etc/resolv.conf "$ROOT/etc/resolv.conf" 2>/dev/null || true

cat > "$ROOT/etc/apt/apt.conf.d/99lean" <<'EOF'
APT::Install-Recommends "false";
APT::Install-Suggests "false";
Acquire::Languages "none";
EOF

# stage the desktop app deb, the compositor binary + session files, and every
# extra deb (themes) into the chroot
cp "$DEB" "$ROOT/tmp/mjdev-desktop.deb"
install -Dm755 "$COMPOSITOR_BIN" "$ROOT/tmp/session/mjdevc"
[ -f "$SESSION_DIR/mjdev-session" ] && install -Dm755 "$SESSION_DIR/mjdev-session" "$ROOT/tmp/session/mjdev-session"
[ -f "$SESSION_DIR/mjdev.desktop" ] && install -Dm644 "$SESSION_DIR/mjdev.desktop" "$ROOT/tmp/session/mjdev.desktop"
if [ -n "$EXTRA_DEBS_DIR" ] && ls "$EXTRA_DEBS_DIR"/*.deb >/dev/null 2>&1; then
    mkdir -p "$ROOT/tmp/extra-debs"
    cp "$EXTRA_DEBS_DIR"/*.deb "$ROOT/tmp/extra-debs/"
fi

# ---- 2-3. provision: kernel + wayland + our debs + theme activation + autostart
cat > "$ROOT/tmp/provision.sh" <<PROVISION
#!/bin/sh
set -eu
export DEBIAN_FRONTEND=noninteractive
echo "$APP_NAME" > /etc/hostname

# enable contrib + non-free + non-free-firmware so the full non-free driver and
# firmware set is installable (minbase only enables main). deb822 or one-line,
# whichever the base wrote.
if [ -f /etc/apt/sources.list.d/debian.sources ]; then
    sed -i 's/^Components:.*/Components: main contrib non-free non-free-firmware/' \
        /etc/apt/sources.list.d/debian.sources
else
    echo "deb $MIRROR $SUITE main contrib non-free non-free-firmware" > /etc/apt/sources.list
fi

apt-get update
# kernel + live boot + bare wayland runtime + plymouth (boot splash). NO xorg,
# NO desktop environment.
apt-get install --no-install-recommends -y \
    linux-image-amd64 live-boot systemd-sysv \
    dbus dbus-user-session seatd \
    plymouth plymouth-label \
    libgl1-mesa-dri libglx-mesa0 libegl-mesa0 \
    fontconfig fonts-dejavu-core
# the wayland compositor runtime stack mjdevc is linked against. libwlroots-0.18 pulls
# its whole chain (libinput, libdrm, libgbm, libseat, libxkbcommon, libwayland-*,
# libdisplay-info, libliftoff, libpixman, ...). XWayland: the AWT-based Compose shell
# needs an X display, which mjdevc provides via its built-in XWayland (this is the
# X-on-wayland shim, NOT a full xorg server). libegl1/libgles2 = the glvnd GL dispatch
# the renderer uses. WITHOUT these mjdevc fails to even load (libwlroots-0.18.so missing)
# -> black screen + tty1 autologin crash-loop.
apt-get install --no-install-recommends -y $RUNTIME_DEPS
# non-free GPU/wifi drivers + firmware so real hardware gets accelerated GL and
# working radios (in a VM mesa still falls back to llvmpipe, which the session
# allows). "|| true": some firmware metapackages are absent on some mirrors —
# never fail the build for them.
apt-get install --no-install-recommends -y \
    mesa-va-drivers mesa-vulkan-drivers || true
apt-get install --no-install-recommends -y \
    firmware-linux firmware-linux-nonfree firmware-misc-nonfree \
    firmware-iwlwifi firmware-realtek firmware-atheros || true

# our desktop deb is built by jpackage on an ubuntu host, so its auto-generated
# Depends carry one ubuntu-only name — libjpeg-turbo8 (ubuntu's libjpeg.so.8),
# which does not exist in debian (debian ships libjpeg62-turbo). the app is
# self-contained (bundled jre + skiko, which carries its own image codecs), so the
# dep is spurious. strip it from the control file before installing — that leaves a
# fully-satisfiable package, so apt resolves the rest normally and never ends up in
# a permanently "broken" state that would block apt autoremove during cleanup.
dpkg-deb -R /tmp/mjdev-desktop.deb /tmp/deb-fix
sed -i -E 's/, *libjpeg-turbo8//; s/libjpeg-turbo8, *//; s/^(Depends:) *libjpeg-turbo8 *\$/\1/' \
    /tmp/deb-fix/DEBIAN/control
# the deb's postinst runs "xdg-desktop-menu install", which fails in a minbase chroot
# (no desktop-environment menu infrastructure). the menu entry is cosmetic — the session
# execs /opt/mjdev-desktop directly — so make that call non-fatal instead of fighting
# xdg-desktop-menu's DE detection. we still drop the .desktop into /usr/share/applications.
mkdir -p /usr/share/applications
sed -i 's/^xdg-desktop-menu install .*/& || true/' /tmp/deb-fix/DEBIAN/postinst
dpkg-deb -b /tmp/deb-fix /tmp/mjdev-desktop-fixed.deb
apt-get install --no-install-recommends -y /tmp/mjdev-desktop-fixed.deb
install -Dm644 /opt/mjdev-desktop/lib/mjdev-desktop-mjdev-desktop.desktop \
    /usr/share/applications/mjdev-desktop.desktop 2>/dev/null || true

# every extra deb from deb-packages/ (plymouth/cursor/sound themes). their deps
# (plymouth, plymouth-label) are already in the base; their postinst scripts register
# the update-alternatives activated below.
if ls /tmp/extra-debs/*.deb >/dev/null 2>&1; then
    apt-get install --no-install-recommends -y /tmp/extra-debs/*.deb
fi

# compositor binary + wayland session launcher + the wayland-sessions entry
install -Dm755 /tmp/session/mjdevc /usr/bin/mjdevc
[ -f /tmp/session/mjdev-session ] && install -Dm755 /tmp/session/mjdev-session /usr/bin/mjdev-session
[ -f /tmp/session/mjdev.desktop ] && install -Dm644 /tmp/session/mjdev.desktop /usr/share/wayland-sessions/mjdev.desktop

# ---- activate the themes -------------------------------------------------
# plymouth: make mjdev the default boot theme and rebuild the initramfs so the
# splash is embedded. -R rebuilds; fall back to the alternative + update-initramfs.
if [ -f /usr/share/plymouth/themes/mjdev/mjdev.plymouth ]; then
    plymouth-set-default-theme -R mjdev 2>/dev/null || {
        update-alternatives --set default.plymouth \
            /usr/share/plymouth/themes/mjdev/mjdev.plymouth || true
        update-initramfs -u || true
    }
fi
# cursor: the postinst registered bloom under x-cursor-theme; select it and
# export it for wayland clients (which read XCURSOR_THEME, not the X alternative).
if [ -f /usr/share/icons/bloom/cursor.theme ]; then
    update-alternatives --set x-cursor-theme /usr/share/icons/bloom/cursor.theme || true
    echo 'XCURSOR_THEME=bloom' >> /etc/environment
fi
# sound: point the freedesktop sound theme at mjdev for libcanberra / our shell.
if [ -d /usr/share/sounds/mjdev ]; then
    echo 'SOUND_THEME=mjdev' >> /etc/environment
    echo 'MJDEV_SOUND_THEME=mjdev' >> /etc/environment
fi

# passwordless live user, autologin tty1, straight into the wayland session
useradd -m -s /bin/bash $LIVE_USER
passwd -d $LIVE_USER
# the compositor opens /dev/dri/card0 and /run/seatd.sock (group "video"); without
# these groups the user gets no DRM seat and the session is a black screen. usermod,
# not adduser — minbase ships usermod (passwd) but not the adduser wrapper.
usermod -aG video,input,render $LIVE_USER
getent group seat >/dev/null 2>&1 && usermod -aG seat $LIVE_USER || true
systemctl enable seatd

mkdir -p /etc/systemd/system/getty@tty1.service.d
cat > /etc/systemd/system/getty@tty1.service.d/autologin.conf <<EOF
[Service]
ExecStart=
ExecStart=-/sbin/agetty --autologin $LIVE_USER --noclear %I \\\$TERM
EOF

cat > /home/$LIVE_USER/.bash_profile <<EOF
# start the mjdev desktop on boot (tty1 only)
if [ -z "\\\$WAYLAND_DISPLAY" ] && [ "\\\$(tty)" = "/dev/tty1" ]; then
    exec mjdev-session
fi
EOF
chown $LIVE_USER:$LIVE_USER /home/$LIVE_USER/.bash_profile
PROVISION
chmod +x "$ROOT/tmp/provision.sh"
chroot "$ROOT" /tmp/provision.sh

# ---- 4. clean_system: strip everything not needed -> smallest image ------
clean_system() {
    echo ">> clean_system: stripping docs, locales, caches, autoremove"
    cat > "$ROOT/tmp/clean.sh" <<'CLEAN'
#!/bin/sh
set -eu
export DEBIAN_FRONTEND=noninteractive
apt-get -y autoremove --purge
apt-get -y clean
rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
rm -rf /usr/share/doc/* /usr/share/man/* /usr/share/info/*
find /usr/share/locale -mindepth 1 -maxdepth 1 ! -name 'en*' ! -name 'cs*' \
    -exec rm -rf {} + 2>/dev/null || true
rm -f /etc/apt/apt.conf.d/99lean
CLEAN
    chmod +x "$ROOT/tmp/clean.sh"
    chroot "$ROOT" /tmp/clean.sh
    rm -rf "$ROOT/tmp/provision.sh" "$ROOT/tmp/clean.sh" \
        "$ROOT/tmp/mjdev-desktop.deb" "$ROOT/tmp/session" "$ROOT/tmp/extra-debs"
}
clean_system

# ---- 5. squash + assemble bootable iso -----------------------------------
echo ">> exporting kernel + initrd"
cp "$ROOT"/boot/vmlinuz-* "$ISO/live/vmlinuz"
cp "$ROOT"/boot/initrd.img-* "$ISO/live/initrd.img"

cleanup
echo ">> mksquashfs (bulk of the time)"
# xz + x86 BCJ filter = smallest squashfs for an amd64 rootfs
mksquashfs "$ROOT" "$ISO/live/filesystem.squashfs" \
    -comp xz -Xbcj x86 -b 1M -noappend -e boot

cat > "$ISO/boot/grub/grub.cfg" <<'EOF'
set default=0
set timeout=3
menuentry "mjdev desktop (live)" {
    linux /live/vmlinuz boot=live quiet splash
    initrd /live/initrd.img
}
EOF

echo ">> grub-mkrescue -> $OUT"
grub-mkrescue -o "$OUT" "$ISO"

# we run as root (pkexec/sudo); hand the iso back to the invoking user so it isn't
# a root-owned file sitting in releases/. PKEXEC_UID (pkexec) / SUDO_UID (sudo).
OWNER_UID="${PKEXEC_UID:-${SUDO_UID:-}}"
[ -n "$OWNER_UID" ] && chown "$OWNER_UID":"$OWNER_UID" "$OUT" 2>/dev/null || true
echo ">> done: $OUT ($(du -h "$OUT" | cut -f1))"

# the iso is built — drop the (root-owned) scratch rootfs so it doesn't pile up
# in /tmp. on failure WORK is kept (the trap only unmounts) for debugging.
rm -rf "$WORK"
