#!/bin/sh
# Switches the display manager started at boot between the psippr login screen (greetd) and gdm.
# Run as root (gradle switchGreeterToPsippr / switchGreeterToGDM do it through pkexec / sudo).
# Takes effect on the next boot: the running display manager, and the session in it, is kept.
#
# usage: switch-greeter.sh psippr|gdm [log-file]
set -eu

TARGET="${1:-}"
LOG="${2:-}"
# pkexec detaches stdio: write everything to the log the caller reads afterwards
[ -n "$LOG" ] && exec >>"$LOG" 2>&1

# systemd units of the display managers
UNIT_DIR=/usr/lib/systemd/system
# the alias systemd starts at boot (display-manager.service -> the chosen one)
DM_LINK=/etc/systemd/system/display-manager.service
# debian's record of the chosen display manager binary
DM_FILE=/etc/X11/default-display-manager
# greetd drop-in of the psippr-desktop deb, running the psippr login screen
PSIPPR_DROP_IN=/etc/systemd/system/greetd.service.d/psippr.conf

case "$TARGET" in
    psippr) UNIT=greetd; BIN=/usr/sbin/greetd; CHOICE=psippr-desktop ;;
    gdm) UNIT=gdm3; BIN=/usr/sbin/gdm3; CHOICE=gdm3 ;;
    *) echo "usage: $0 psippr|gdm [log-file]"; exit 2 ;;
esac

echo "switch-greeter: $(date '+%F %T') -> $TARGET"
[ -e "$UNIT_DIR/$UNIT.service" ] || { echo "switch-greeter: $UNIT is not installed"; exit 1; }
[ -x "$BIN" ] || { echo "switch-greeter: $BIN not found"; exit 1; }
if [ "$TARGET" = psippr ] && [ ! -f "$PSIPPR_DROP_IN" ]; then
    echo "switch-greeter: $PSIPPR_DROP_IN missing - install the psippr-desktop deb first"
    exit 1
fi

mkdir -p "$(dirname "$DM_FILE")"
echo "$BIN" >"$DM_FILE"
# the same answer the "Default display manager" install dialog stores
echo "$CHOICE shared/default-x-display-manager select $CHOICE" | debconf-set-selections
ln -sf "$UNIT_DIR/$UNIT.service" "$DM_LINK"
systemctl daemon-reload

echo "switch-greeter: display manager at next boot: $UNIT ($(readlink "$DM_LINK"))"
