#!/usr/bin/env bash
# Boots the mjdev live ISO in QEMU (headless, KVM) and verifies the wayland desktop
# actually starts: captures the session log to ./log.txt and screenshots to /tmp/iso-test,
# then prints PASS/FAIL by inspecting the log. No ISO rebuild — tests an existing ISO.
#
#   .claude/skills/mjdev-qemu-test/test-desktop.sh [ISO] [--shots]
#     ISO       path to the live iso (default: releases/<app>-<ver>.iso)
#     --shots   also capture screenshots via QMP (needs qmp_drive.py alongside)
#
# What it checks in ~/.cache/mjdev-desktop/session.log:
#   PASS  -> "shell started" present AND no "RenderException"/"cannot open shared object"
#   FAIL  -> prints the first error line (missing lib / renderer / Skiko GL context / ...)
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../../.." && pwd)"        # project root (.claude/skills/<name>/ -> root)
CATALOG="$ROOT/gradle/libs.versions.toml"
rc(){ sed -n "s/^$1 *= *\"\(.*\)\"/\1/p" "$CATALOG"; }
APP="$(rc app-name)"; VER="$(rc app-pkg-version)"

ISO=""; SHOTS=0
for a in "$@"; do case "$a" in --shots) SHOTS=1;; *) ISO="$a";; esac; done
ISO="${ISO:-$ROOT/releases/$APP-$VER.iso}"
[ -f "$ISO" ] || { echo "iso not found: $ISO (build with ./gradlew makeIso)"; exit 1; }
WORK=/tmp/iso-test; mkdir -p "$WORK"
command -v qemu-system-x86_64 >/dev/null || { echo "need qemu-system-x86_64"; exit 1; }

echo ">> extracting kernel/initrd from $(basename "$ISO")"
xorriso -osirrox on -indev "$ISO" \
  -extract /live/vmlinuz "$WORK/vmlinuz" -extract /live/initrd.img "$WORK/initrd.img" 2>/dev/null

KVM=(); [ -w /dev/kvm ] && KVM=(-enable-kvm -cpu host)

# --- phase 1: serial login -> dump session.log to project-root log.txt ---
echo ">> booting (serial) to capture session.log ..."
{
  sleep 30; printf '\n\nmjdev\n'; sleep 4
  printf 'echo ===LOGSTART===; cat ~/.cache/mjdev-desktop/session.log.1 2>/dev/null; echo "[--- current ---]"; cat ~/.cache/mjdev-desktop/session.log 2>/dev/null; echo ===LOGEND===\n'
  sleep 7
} | timeout 80 qemu-system-x86_64 "${KVM[@]}" -m 4096 -smp 4 \
  -kernel "$WORK/vmlinuz" -initrd "$WORK/initrd.img" -append "boot=live console=ttyS0,115200" \
  -cdrom "$ISO" -nographic -serial mon:stdio > "$WORK/serial.raw" 2>&1 || true
tr -d '\r' < "$WORK/serial.raw" | sed 's/\x1b\[[0-9;?]*[a-zA-Z]//g' \
  | sed -n '/===LOGSTART===/,/===LOGEND===/p' > "$ROOT/log.txt"

# --- phase 2 (optional): screenshots via QMP ---
if [ "$SHOTS" = 1 ] && [ -f "$HERE/qmp_drive.py" ]; then
  echo ">> booting (framebuffer) for screenshots ..."
  rm -f "$WORK/qmp.sock"
  qemu-system-x86_64 "${KVM[@]}" -m 4096 -smp 4 \
    -kernel "$WORK/vmlinuz" -initrd "$WORK/initrd.img" -append "boot=live console=ttyS0,115200" \
    -cdrom "$ISO" -device virtio-vga -usb -device usb-tablet \
    -qmp "unix:$WORK/qmp.sock,server,nowait" -display none > "$WORK/qemu-shots.log" 2>&1 &
  python3 "$HERE/qmp_drive.py" "$WORK/qmp.sock" || true
fi

# --- verdict ---
echo "================ session.log -> log.txt ================"
sed -n '1,40p' "$ROOT/log.txt"
echo "========================================================"
if grep -q "shell started" "$ROOT/log.txt" \
   && ! grep -qiE "RenderException|cannot open shared object|failed to create wlr" "$ROOT/log.txt"; then
  echo "RESULT: PASS — compositor + shell started, no fatal render/link error"
else
  echo "RESULT: FAIL — first error:"
  grep -m1 -iE "RenderException|cannot open shared object|failed to create|EGL_BAD|error" "$ROOT/log.txt" || echo "  (no obvious error line; inspect log.txt)"
fi
[ "$SHOTS" = 1 ] && ls -la "$WORK"/*.png 2>/dev/null || true
