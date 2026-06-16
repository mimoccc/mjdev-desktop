#!/usr/bin/env bash
# Automated window/decoration/theme test against a running mjdevc.
# Usage: compositor/scripts/window-deco-test.sh [logfile]
set -u

SOCK="${MJDEV_SOCK:-${XDG_RUNTIME_DIR:-/run/user/$(id -u)}/mjdev-compositor.sock}"
LOG="${1:-/tmp/mjdev-window-deco-test.log}"

exec > >(tee -a "$LOG") 2>&1
echo "== mjdev window/deco test $(date -Is) =="

python3 - "$SOCK" <<'PY'
import json, socket, sys, time

SOCK = sys.argv[1]

def wait_sock(timeout=90):
    import os
    for _ in range(timeout):
        if os.path.exists(SOCK):
            return
        time.sleep(1)
    print(f"FAIL: no socket at {SOCK}")
    sys.exit(1)

def ipc(cmd):
    s = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    s.settimeout(5)
    s.connect(SOCK)
    s.sendall((json.dumps(cmd) + "\n").encode())
    s.shutdown(socket.SHUT_WR)
    data = b""
    while True:
        try:
            chunk = s.recv(65536)
            if not chunk:
                break
            data += chunk
        except socket.timeout:
            break
    s.close()
    if not data:
        return {}
    return json.loads(data.decode().split("\n")[0])

wait_sock()
print(f"socket: {SOCK}")

wins = ipc({"cmd": "list-windows"}).get("windows", [])
print("--- list-windows ---")
for w in wins:
    print(f"  id={w['id']} app={w.get('app_id')} {w['x']},{w['y']} {w['width']}x{w['height']}")

calc = next((w for w in wins if "calculator" in (w.get("app_id") or "").lower()), None)
if calc is None:
    print("FAIL: org.gnome.Calculator not listed (start with --startup gnome-calculator)")
    sys.exit(1)

fail = 0
if calc["y"] < 0:
    print(f"FAIL: negative Y={calc['y']}")
    fail = 1
else:
    print(f"OK: geometry y={calc['y']} size={calc['width']}x{calc['height']}")

cid = calc["id"]
x, y, w, h = calc["x"], calc["y"], calc["width"], calc["height"]

r = ipc({"cmd": "set-decoration-theme", "bg_r": 180, "bg_g": 40, "bg_b": 40,
         "fg_r": 255, "fg_g": 255, "fg_b": 255,
         "icon_bg_r": 220, "icon_bg_g": 80, "icon_bg_b": 80,
         "icon_fg_r": 255, "icon_fg_g": 200, "icon_fg_b": 200})
if not r.get("ok"):
    print(f"FAIL: set-decoration-theme: {r}")
    fail = 1
else:
    print("OK: set-decoration-theme")

# unmaximize if needed so titlebar close target is predictable
if calc.get("maximized"):
    ipc({"cmd": "maximize", "id": cid, "maximized": False})
    time.sleep(0.4)
    calc = ipc({"cmd": "list-windows"}).get("windows", [])[0]
    x, y, w, h = calc["x"], calc["y"], calc["width"], calc["height"]

ipc({"cmd": "activate", "id": cid})
close_x = x + w - 18
tb_y = y + 18
ipc({"cmd": "click", "x": close_x, "y": tb_y})
time.sleep(0.5)

still = any(w["id"] == cid for w in ipc({"cmd": "list-windows"}).get("windows", []))
if still:
    print("FAIL: titlebar close click did not close window")
    fail = 1
else:
    print("OK: titlebar close via IPC click")

sys.exit(fail)
PY

echo "exit=$?"
