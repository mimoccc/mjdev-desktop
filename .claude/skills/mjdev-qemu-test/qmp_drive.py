#!/usr/bin/env python3
# Drive a running QEMU via its QMP unix socket to test the mjdev wayland desktop.
# Screendumps boot frames, then reveals the bottom bar (pointer -> bottom edge) and the
# control center (pointer -> right edge), dumping each, then quits the VM. PPM->PNG is
# built in (no deps) so frames can be viewed with the Read tool.
#
#   python3 qmp_drive.py /tmp/iso-test/qmp.sock [/out/dir]
# Requires the VM launched with: -qmp unix:<sock>,server,nowait  and a tablet:
#   -usb -device usb-tablet   (absolute pointer; abs axes are 0..32767)
import socket, json, sys, time, struct, zlib, os

SOCK = sys.argv[1] if len(sys.argv) > 1 else "/tmp/iso-test/qmp.sock"
OUT = sys.argv[2] if len(sys.argv) > 2 else "/tmp/iso-test"

class QMP:
    def __init__(self, path):
        self.s = socket.socket(socket.AF_UNIX)
        for _ in range(120):
            try:
                self.s.connect(path); break
            except OSError:
                time.sleep(1)
        else:
            raise SystemExit("no QMP socket: " + path)
        self.f = self.s.makefile("rwb", buffering=0)
        self._read(); self.cmd("qmp_capabilities")
    def _read(self):
        while True:
            line = self.f.readline()
            if not line: raise EOFError("QMP closed")
            obj = json.loads(line)
            if "event" in obj: continue
            return obj
    def cmd(self, exe, **a):
        m = {"execute": exe}
        if a: m["arguments"] = a
        self.f.write((json.dumps(m) + "\n").encode()); return self._read()
    def dump(self, p): self.cmd("screendump", filename=p)
    def move(self, xf, yf):
        self.cmd("input-send-event", events=[
            {"type": "abs", "data": {"axis": "x", "value": int(xf*32767)}},
            {"type": "abs", "data": {"axis": "y", "value": int(yf*32767)}}])

def ppm2png(ppm, png):
    d = open(ppm, "rb").read()
    if d[:2] != b"P6": raise ValueError("not P6")
    idx, vals = 2, []
    while len(vals) < 3:
        while d[idx] in b" \t\n\r": idx += 1
        s = idx
        while d[idx] not in b" \t\n\r": idx += 1
        vals.append(int(d[s:idx]))
    w, h, _ = vals; idx += 1
    raw = d[idx:idx+w*h*3]
    def ch(t, x): return struct.pack(">I", len(x)) + t + x + struct.pack(">I", zlib.crc32(t+x) & 0xffffffff)
    rows = bytearray()
    for y in range(h):
        rows.append(0); rows += raw[y*w*3:(y+1)*w*3]
    open(png, "wb").write(b"\x89PNG\r\n\x1a\n" + ch(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 2, 0, 0, 0))
                          + ch(b"IDAT", zlib.compress(bytes(rows), 6)) + ch(b"IEND", b""))
    return w, h

def variation(ppm):
    d = open(ppm, "rb").read(); body = d[15:15+60000]
    return (max(body) - min(body)) if body else 0

def shot(q, name, t):
    ppm, png = f"{OUT}/{name}.ppm", f"{OUT}/{name}.png"
    q.dump(ppm)
    try:
        w, h = ppm2png(ppm, png)
        print(f"[{t:>4}s] {name}.png {w}x{h} var={variation(ppm)}", flush=True)
    except Exception as e:
        print(f"[{t:>4}s] {name}: {e}", flush=True)

def main():
    os.makedirs(OUT, exist_ok=True)
    q = QMP(SOCK); print(">> QMP connected", flush=True)
    t0 = time.time(); el = lambda: int(time.time()-t0)
    for t in (60, 100, 140, 180, 220):
        while el() < t: time.sleep(1)
        shot(q, f"boot-{t:03d}s", el())
    q.move(0.5, 0.999); time.sleep(3); shot(q, "bottombar", el())
    q.move(0.999, 0.5); time.sleep(3); shot(q, "controlcenter", el())
    q.move(0.5, 0.5); time.sleep(2); shot(q, "final", el())
    try: q.cmd("quit")
    except Exception: pass
    print(">> done", flush=True)

main()
