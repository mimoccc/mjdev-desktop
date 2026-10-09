#!/usr/bin/env bash
# Builds psippr-desktop-<version>.iso: a minimal, bootable Debian (latest stable)
# live image carrying only wayland + the psippr desktop — no X server, no desktop
# environment, no recommends. It enables the non-free driver/firmware set, installs
# every *.deb from deb-packages/ (plymouth + cursor + sound themes) and activates
# them, and autostarts the psippr wayland session on boot.
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
# wayland runtime stack for psipprc — single source of truth in the version catalog (not hardcoded)
RUNTIME_DEPS="$(read_catalog app-compositor-runtime-deps)"
# apt-get must wait for the dpkg lock instead of failing ("Could not get lock
# /var/lib/dpkg/lock-frontend") when another apt/dpkg runs — value from the catalog.
APT_LOCK_TIMEOUT="$(read_catalog app-apt-lock-timeout)"

# ---- config / args -------------------------------------------------------
# trixie = Debian 13, the current stable ("latest public version"). A bare
# "stable" can symlink to sid on non-debian hosts, so a concrete codename is used.
SUITE="${PSIPPR_ISO_SUITE:-trixie}"
MIRROR="${PSIPPR_ISO_MIRROR:-http://deb.debian.org/debian}"
# building a debian rootfs from a non-debian host needs the debian archive key
KEYRING="/usr/share/keyrings/debian-archive-keyring.gpg"
# the gradle task overrides these; the defaults match the in-tree build outputs
DEB="${PSIPPR_ISO_DEB:-$(ls "$HERE"/packages/main-release/deb/*.deb 2>/dev/null | head -n1 || true)}"
COMPOSITOR_BIN="${PSIPPR_ISO_COMPOSITOR_BIN:-$HERE/compositor/build/session-install/psipprc}"
SESSION_DIR="${PSIPPR_ISO_SESSION_DIR:-$HERE/compositor/build/session-install}"
EXTRA_DEBS_DIR="${PSIPPR_ISO_EXTRA_DEBS:-$HERE/deb-packages}"
OUT="${PSIPPR_ISO_OUT:-$HERE/releases/$APP_NAME-$VERSION.iso}"
LIVE_USER="psippr"
# default account of the image: user psippr / password psippr, a system administrator (sudo)
LIVE_PASSWORD="${PSIPPR_ISO_PASSWORD:-psippr}"
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
LOG="${PSIPPR_ISO_LOG:-/tmp/psippr-iso-build.log}"
exec > >(tee "$LOG") 2>&1
echo ">> psippr iso build log -> $LOG"

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
WORK="${WORK:-$(mktemp -d /tmp/psippr-iso.XXXXXX)}"
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

cat > "$ROOT/etc/apt/apt.conf.d/99lean" <<EOF
APT::Install-Recommends "false";
APT::Install-Suggests "false";
Acquire::Languages "none";
DPkg::Lock::Timeout "$APT_LOCK_TIMEOUT";
EOF

# stage the desktop app deb, the compositor binary + session files, and every
# extra deb (themes) into the chroot
cp "$DEB" "$ROOT/tmp/psippr-desktop.deb"
install -Dm755 "$COMPOSITOR_BIN" "$ROOT/tmp/session/psipprc"
[ -f "$SESSION_DIR/psippr-session" ] && install -Dm755 "$SESSION_DIR/psippr-session" "$ROOT/tmp/session/psippr-session"
[ -f "$SESSION_DIR/psippr.desktop" ] && install -Dm644 "$SESSION_DIR/psippr.desktop" "$ROOT/tmp/session/psippr.desktop"
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
# the wayland compositor runtime stack psipprc is linked against. libwlroots-0.18 pulls
# its whole chain (libinput, libdrm, libgbm, libseat, libxkbcommon, libwayland-*,
# libdisplay-info, libliftoff, libpixman, ...). XWayland: the AWT-based Compose shell
# needs an X display, which psipprc provides via its built-in XWayland (this is the
# X-on-wayland shim, NOT a full xorg server). libegl1/libgles2 = the glvnd GL dispatch
# the renderer uses. WITHOUT these psipprc fails to even load (libwlroots-0.18.so missing)
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
dpkg-deb -R /tmp/psippr-desktop.deb /tmp/deb-fix
sed -i -E 's/, *libjpeg-turbo8//; s/libjpeg-turbo8, *//; s/^(Depends:) *libjpeg-turbo8 *\$/\1/' \
    /tmp/deb-fix/DEBIAN/control
# the deb's postinst runs "xdg-desktop-menu install", which fails in a minbase chroot
# (no desktop-environment menu infrastructure). the menu entry is cosmetic — the session
# execs /opt/psippr-desktop directly — so make that call non-fatal instead of fighting
# xdg-desktop-menu's DE detection. we still drop the .desktop into /usr/share/applications.
mkdir -p /usr/share/applications
sed -i 's/^xdg-desktop-menu install .*/& || true/' /tmp/deb-fix/DEBIAN/postinst
dpkg-deb -b /tmp/deb-fix /tmp/psippr-desktop-fixed.deb
# the image has no other display manager: answer the deb's "Default display manager" question
# with psippr-desktop, so greetd shows the psippr login screen at boot
echo "psippr-desktop shared/default-x-display-manager select psippr-desktop" | debconf-set-selections
apt-get install --no-install-recommends -y /tmp/psippr-desktop-fixed.deb
install -Dm644 /opt/psippr-desktop/lib/psippr-desktop-psippr-desktop.desktop \
    /usr/share/applications/psippr-desktop.desktop 2>/dev/null || true

# every extra deb from deb-packages/ (plymouth/cursor/sound themes). their deps
# (plymouth, plymouth-label) are already in the base; their postinst scripts register
# the update-alternatives activated below.
if ls /tmp/extra-debs/*.deb >/dev/null 2>&1; then
    apt-get install --no-install-recommends -y /tmp/extra-debs/*.deb
fi

# compositor binary + wayland session launcher + the wayland-sessions entry
install -Dm755 /tmp/session/psipprc /usr/bin/psipprc
[ -f /tmp/session/psippr-session ] && install -Dm755 /tmp/session/psippr-session /usr/bin/psippr-session
[ -f /tmp/session/psippr.desktop ] && install -Dm644 /tmp/session/psippr.desktop /usr/share/wayland-sessions/psippr.desktop

# ---- activate the themes -------------------------------------------------
# plymouth: make psippr the default boot theme and rebuild the initramfs so the
# splash is embedded. -R rebuilds; fall back to the alternative + update-initramfs.
if [ -f /usr/share/plymouth/themes/psippr/psippr.plymouth ]; then
    plymouth-set-default-theme -R psippr 2>/dev/null || {
        update-alternatives --set default.plymouth \
            /usr/share/plymouth/themes/psippr/psippr.plymouth || true
        update-initramfs -u || true
    }
fi
# cursor: the postinst registered bloom under x-cursor-theme; select it and
# export it for wayland clients (which read XCURSOR_THEME, not the X alternative).
if [ -f /usr/share/icons/bloom/cursor.theme ]; then
    update-alternatives --set x-cursor-theme /usr/share/icons/bloom/cursor.theme || true
    echo 'XCURSOR_THEME=bloom' >> /etc/environment
fi
# sound: point the freedesktop sound theme at psippr for libcanberra / our shell.
if [ -d /usr/share/sounds/psippr ]; then
    echo 'SOUND_THEME=psippr' >> /etc/environment
    echo 'PSIPPR_SOUND_THEME=psippr' >> /etc/environment
fi

# old: passwordless live user, autologin tty1 straight into the session - no login screen
## passwordless live user, autologin tty1, straight into the wayland session
#useradd -m -s /bin/bash $LIVE_USER
#passwd -d $LIVE_USER
## the compositor opens /dev/dri/card0 and /run/seatd.sock (group "video"); without
## these groups the user gets no DRM seat and the session is a black screen. usermod,
## not adduser — minbase ships usermod (passwd) but not the adduser wrapper.
#usermod -aG video,input,render $LIVE_USER
#getent group seat >/dev/null 2>&1 && usermod -aG seat $LIVE_USER || true
#systemctl enable seatd
#
#mkdir -p /etc/systemd/system/getty@tty1.service.d
#cat > /etc/systemd/system/getty@tty1.service.d/autologin.conf <<EOF
#[Service]
#ExecStart=
#ExecStart=-/sbin/agetty --autologin $LIVE_USER --noclear %I \\\$TERM
#EOF
#
#cat > /home/$LIVE_USER/.bash_profile <<EOF
## start the psippr desktop on boot (tty1 only)
#if [ -z "\\\$WAYLAND_DISPLAY" ] && [ "\\\$(tty)" = "/dev/tty1" ]; then
#    exec psippr-session
#fi
#EOF
#chown $LIVE_USER:$LIVE_USER /home/$LIVE_USER/.bash_profile

# default account psippr / psippr, system administrator; login through the psippr login screen
# (greetd, enabled by the deb's display manager choice above)
apt-get install --no-install-recommends -y sudo
useradd -m -s /bin/bash $LIVE_USER
echo "$LIVE_USER:$LIVE_PASSWORD" | chpasswd
# the compositor opens /dev/dri/card0 and /run/seatd.sock (group "video"); without
# these groups the user gets no DRM seat and the session is a black screen. usermod,
# not adduser — minbase ships usermod (passwd) but not the adduser wrapper.
usermod -aG sudo,video,input,render $LIVE_USER
getent group seat >/dev/null 2>&1 && usermod -aG seat $LIVE_USER || true
# the greeter runs psipprc as _greetd: it needs the same seat / GPU groups
usermod -aG video,input,render _greetd
getent group seat >/dev/null 2>&1 && usermod -aG seat _greetd || true
systemctl enable seatd
systemctl set-default graphical.target
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
        "$ROOT/tmp/psippr-desktop.deb" "$ROOT/tmp/session" "$ROOT/tmp/extra-debs"
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
menuentry "psippr desktop (live)" {
    linux /live/vmlinuz boot=live quiet splash
    initrd /live/initrd.img
}
EOF

echo ">> grub-mkrescue -> $OUT"
grub-mkrescue -o "$OUT" "$ISO"

# we run as root (pkexec/sudo); hand the iso AND its directory back to the invoking
# user — mkdir -p above may have created releases/ as root, which then blocks the
# user's collectReleases ("Could not copy ... Operace zamítnuta") on the next build.
# PKEXEC_UID (pkexec) / SUDO_UID (sudo).
OWNER_UID="${PKEXEC_UID:-${SUDO_UID:-}}"
[ -n "$OWNER_UID" ] && chown "$OWNER_UID":"$OWNER_UID" "$OUT" "$(dirname "$OUT")" 2>/dev/null || true
echo ">> done: $OUT ($(du -h "$OUT" | cut -f1))"

# the iso is built — drop the (root-owned) scratch rootfs so it doesn't pile up
# in /tmp. on failure WORK is kept (the trap only unmounts) for debugging.
rm -rf "$WORK"
