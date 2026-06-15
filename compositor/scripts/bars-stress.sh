#!/usr/bin/env bash
# Randomized stress test for the shell bars (dock / control-center / apps menu), driven entirely
# through the mjdev compositor IPC socket — no real mouse needed. It injects pointer moves, clicks
# and keys in randomized cycles mimicking real use:
#   reveal dock, open menu (click menu icon), hover control center, click desktop centre,
#   launch an app from the dock, close launched app windows.
# It does NOT assert by itself — read the shell log afterwards (analyze section below) for
# size flip-flops, position drift, show/hide floods and jams.
#
# Usage: bars-stress.sh [cycles]   (default 32). Requires a running nested/real mjdevc.
set -u

SOCK="${MJDEV_SOCK:-${XDG_RUNTIME_DIR:-/run/user/$(id -u)}/mjdev-compositor.sock}"
CYCLES="${1:-32}"
W=1280; H=720                       # nested output (MJDEVC_OUTPUT)
ESC=1                               # evdev KEY_ESC

[ -S "$SOCK" ] || { echo "no compositor socket at $SOCK"; exit 1; }

# send one or more newline-terminated JSON commands on a single connection, hold it briefly so the
# server processes the buffer before EOF (half-close race), return the response on stdout.
ipc() { { printf '%s\n' "$@"; sleep 0.3; } | nc -U "$SOCK" 2>/dev/null; }

rnd() { echo $(( RANDOM % ($2 - $1 + 1) + $1 )); }

reveal_dock()  { ipc "{\"cmd\":\"pointer-move\",\"x\":$(rnd 100 $((W-100))),\"y\":$((H-2))}" >/dev/null; }
move_off()     { ipc "{\"cmd\":\"pointer-move\",\"x\":$(rnd 300 900),\"y\":$(rnd 150 400)}" >/dev/null; }
open_menu()    { ipc "{\"cmd\":\"click\",\"x\":40,\"y\":$((H-40))}" >/dev/null; }     # menu icon, dock far-left
esc()          { ipc "{\"cmd\":\"key\",\"code\":$ESC}" >/dev/null; }
hover_cc()     { ipc "{\"cmd\":\"pointer-move\",\"x\":$((W-2)),\"y\":$(rnd 50 $((H-50)))}" >/dev/null; }
click_centre() { ipc "{\"cmd\":\"click\",\"x\":$((W/2)),\"y\":$((H/2))}" >/dev/null; }
launch_app()   { ipc "{\"cmd\":\"pointer-move\",\"x\":$(rnd 120 900),\"y\":$((H-2))}" \
                     "{\"cmd\":\"click\",\"x\":$(rnd 120 900),\"y\":$((H-40))}" >/dev/null; }

close_app_windows() {
  # close every listed (app) window by id so launches don't pile up
  local ids
  ids=$(ipc '{"cmd":"list-windows"}' | grep -oE '"id":[0-9]+' | grep -oE '[0-9]+')
  for id in $ids; do ipc "{\"cmd\":\"close\",\"id\":$id}" >/dev/null; sleep 0.2; done
}

ACTIONS=(reveal_dock open_menu esc hover_cc click_centre launch_app move_off)

echo "stress: $CYCLES cycles via $SOCK"
for ((i=1; i<=CYCLES; i++)); do
  # 2-3 random actions per cycle
  n=$(rnd 2 3)
  line="cycle $i:"
  for ((k=0; k<n; k++)); do
    a=${ACTIONS[$(rnd 0 $((${#ACTIONS[@]}-1)))]}
    line="$line $a"
    "$a"
    sleep 0.25
  done
  echo "$line"
  # periodically reap any apps we launched
  if (( i % 6 == 0 )); then close_app_windows; fi
done

# settle, final cleanup, park pointer in the centre
sleep 1
close_app_windows
ipc "{\"cmd\":\"pointer-move\",\"x\":$((W/2)),\"y\":$((H/2))}" >/dev/null
echo "stress: done"
