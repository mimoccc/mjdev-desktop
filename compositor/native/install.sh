#!/bin/sh
# installs the mjdev compositor + wayland session (run with sudo)
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
install -Dm755 "$DIR/mjdevc" /usr/local/bin/mjdevc
install -Dm755 "$DIR/mjdev-session" /usr/local/bin/mjdev-session
install -Dm644 "$DIR/mjdev.desktop" /usr/share/wayland-sessions/mjdev.desktop
echo "mjdev session installed - select 'Mjdev Desktop' on the login screen"
