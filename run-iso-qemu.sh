#!/usr/bin/env bash
# Boots mjdev-desktop-<version>.iso in QEMU so the wayland desktop can be tried
# without touching the host session. Uses KVM and virtio-gpu GL acceleration
# when available (the desktop needs GL), falls back to plain emulation.
#
#   ./run-iso-qemu.sh                 # boots releases/<app>-<version>.iso (local gtk window)
#   ./run-iso-qemu.sh path/to.iso     # boots a specific image
#   ./run-iso-qemu.sh --vnc           # headless: expose the screen over VNC for remote
#                                     # grab + control (127.0.0.1:5900). egl-headless keeps
#                                     # GL working without a host display. Connect with any
#                                     # VNC client / vncsnapshot / gvncviewer.
#   MJDEV_QEMU_VNC=1 ./run-iso-qemu.sh           # same via env
#   MJDEV_QEMU_VNC_DISPLAY=2 ./run-iso-qemu.sh --vnc   # 127.0.0.1:5902
#   MJDEV_QEMU_VNC_HOST=0.0.0.0 ./run-iso-qemu.sh --vnc  # listen on all interfaces
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
CATALOG="$HERE/gradle/libs.versions.toml"
read_catalog() { sed -n "s/^$1 *= *\"\(.*\)\"/\1/p" "$CATALOG"; }
APP_NAME="$(read_catalog app-name)"
VERSION="$(read_catalog app-pkg-version)"

VNC="${MJDEV_QEMU_VNC:-0}"
ISO=""
for a in "$@"; do
    case "$a" in
        --vnc) VNC=1;;
        *) ISO="$a";;
    esac
done
ISO="${ISO:-$HERE/releases/$APP_NAME-$VERSION.iso}"
RAM="${MJDEV_QEMU_RAM:-4096}"
CPUS="${MJDEV_QEMU_CPUS:-4}"
VNC_HOST="${MJDEV_QEMU_VNC_HOST:-127.0.0.1}"
VNC_DISPLAY="${MJDEV_QEMU_VNC_DISPLAY:-0}"

command -v qemu-system-x86_64 >/dev/null 2>&1 || { echo "qemu-system-x86_64 not found (apt install qemu-system-x86)"; exit 1; }
[ -f "$ISO" ] || { echo "iso not found: $ISO"; echo "build it first: ./gradlew makeIso"; exit 1; }

ARGS=(
    -m "$RAM"
    -smp "$CPUS"
    -cdrom "$ISO"
    -boot d
)

# hardware acceleration when the host exposes it
if [ -w /dev/kvm ]; then
    ARGS+=(-enable-kvm -cpu host)
    echo ">> KVM enabled"
else
    echo ">> /dev/kvm not available - software emulation (slow)"
fi

if [ "$VNC" = "1" ]; then
    # headless screen grab + control over VNC. egl-headless renders the guest GPU
    # output (so the wayland compositor still gets real GL) and hands the framebuffer
    # to qemu's built-in VNC server; VNC injects keyboard/pointer back into the guest.
    # Independent of the guest compositor — no in-image vnc server / wlroots screencopy
    # needed. Listens on $VNC_HOST:(5900+$VNC_DISPLAY).
    if qemu-system-x86_64 -display help 2>/dev/null | grep -q egl-headless; then
        ARGS+=(-device virtio-vga-gl -display "egl-headless,gl=on")
    else
        echo ">> egl-headless unavailable - VNC without GL (compositor falls back to llvmpipe)"
        ARGS+=(-device virtio-vga)
    fi
    ARGS+=(-vnc "$VNC_HOST:$VNC_DISPLAY")
    echo ">> VNC: connect to $VNC_HOST:$((5900 + VNC_DISPLAY))  (e.g. vncviewer $VNC_HOST:$VNC_DISPLAY)"
# virtio-gpu with GL gives the wayland compositor a real GPU path; gtk display
# with gl=on renders it on the host. Fall back to a plain virtio-gpu if the
# host qemu has no GL display backend.
elif qemu-system-x86_64 -display help 2>/dev/null | grep -q gtk; then
    ARGS+=(-device virtio-vga-gl -display gtk,gl=on)
else
    ARGS+=(-device virtio-vga -display sdl)
fi

echo ">> booting $ISO"
exec qemu-system-x86_64 "${ARGS[@]}"
