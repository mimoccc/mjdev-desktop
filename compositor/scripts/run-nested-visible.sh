#!/usr/bin/env bash
# Launches the nested mjdev compositor VISIBLY (window on the user's screen) with the
# desktop shell + a test app. Run this inside a visible terminal so the user can watch
# it live. Logs are tee'd so they can also be inspected.
set -u
cd /home/mimo/Plocha/mjdev-desktop
export DISPLAY=:0 WLR_BACKENDS=x11 MJDEVC_OUTPUT=1280x720
for f in /run/user/1000/.mutter-Xwaylandauth.*; do [ -f "$f" ] && export XAUTHORITY="$f"; done
unset WAYLAND_DISPLAY
BIN=compositor/build/bin/linuxX64/mjdevcReleaseExecutable/mjdevc.kexe
LAUNCH=packages/main/app/mjdev-desktop/bin/mjdev-desktop
echo "== launching mjdevc =="
"$BIN" --shell-cmd "$LAUNCH" --startup "sleep 9; exec gnome-calculator" 2>&1 \
  | tee /home/mimo/Plocha/mjdev-desktop/compositor/build/nested.log
