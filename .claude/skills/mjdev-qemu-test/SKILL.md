---
name: mjdev-qemu-test
description: Boot and test the mjdev wayland desktop (the make-iso ISO or the installed deb) in QEMU headlessly — capture screenshots + session logs, diagnose why the session does not start, iterate live WITHOUT rebuilding the ISO, and reflect verified fixes back into the project (make-iso.sh / session / deb packaging). Use when asked to run/test the desktop ISO in QEMU, screenshot the booted desktop, or debug "black screen / session won't start / desktop not running" on the live image or after installDesktop.
---

# mjdev desktop — QEMU test & debug loop

Goal: the **deb and the ISO must make the mjdev wayland desktop usable on any machine**.
Iterate **without** rebuilding the ISO each time (a full `makeIso` is ~10 min). Only rebuild
for a final check. Reflect every working change back into the project.

## Architecture (what runs)
- `mjdevc` = the wayland compositor (Kotlin/Native, links `libwlroots-0.18` + the wayland stack).
- `mjdev-session` (`session/mjdev-session`) = the session launcher: sets env, `exec mjdevc --session --shell-cmd /opt/mjdev-desktop/bin/mjdev-desktop`, logs to `~/.cache/mjdev-desktop/session.log`.
- shell = the Compose Desktop app (`/opt/mjdev-desktop`). It is **AWT-based → needs an X display**,
  which `mjdevc` provides via its built-in **XWayland** (shim.c:1351, `setenv DISPLAY` at 1368-1374).
  This is *not* xorg; "pure wayland" still needs XWayland for this one legacy app.
- On tty1 the ISO autologins `mjdev` → `mjdev-session`. If it crashes, agetty relogs → crash-loop → black screen.

## Known root causes (check these first)
1. **`libwlroots-0.18.so: cannot open shared object file`** — the runtime wayland stack mjdevc
   links is not installed. The jpackage deb does NOT pull it. FIX: install/depend on
   `libwlroots-0.18 xwayland libegl1 libgles2 libgbm1 libinput10 libseat1 libxkbcommon0`
   (libwlroots-0.18 pulls libdrm/libwayland/libdisplay-info/libliftoff/libpixman/... as its deps).
2. **`Software rendering detected ... WLR_RENDERER_ALLOW_SOFTWARE`** — in a VM (no GPU) mesa is
   llvmpipe and wlroots refuses software GL. FIX: `export WLR_RENDERER_ALLOW_SOFTWARE=1` in
   `session/mjdev-session` (already there; ignored on real GPUs). Needs `libgl1-mesa-dri`.
3. **`java.awt.HeadlessException: No X11 DISPLAY`** — only when the shell is run with no compositor
   (e.g. a bare serial shell). Under mjdevc with XWayland+DISPLAY it is fine. Not a real bug by itself.

Map mjdevc's deps to packages on the (Debian trixie) host:
`ldd compositor/build/session-install/mjdevc | awk '/=>/{print $3}' | while read f; do dpkg -S "$(readlink -f "$f")"; done`

## Always log the failure
`mjdev-session` redirects mjdevc+shell to `~/.cache/mjdev-desktop/session.log` (and `.log.1` = previous
crash). Without that the tty1 session dies to a black screen with no trace. Keep this.

## Fast capture (screenshot + input) — `qmp_drive.py`
Boot QEMU headless with a framebuffer + QMP + a USB tablet, then drive it:
```sh
ISO=releases/mjdev-desktop-1.0.3.iso
# direct-kernel boot is faster than -boot d; extract once:
xorriso -osirrox on -indev "$ISO" -extract /live/vmlinuz /tmp/iso-test/vmlinuz -extract /live/initrd.img /tmp/iso-test/initrd.img
qemu-system-x86_64 -enable-kvm -cpu host -m 4096 -smp 4 \
  -kernel /tmp/iso-test/vmlinuz -initrd /tmp/iso-test/initrd.img -append "boot=live console=ttyS0,115200" \
  -cdrom "$ISO" -device virtio-vga -usb -device usb-tablet \
  -qmp unix:/tmp/iso-test/qmp.sock,server,nowait -serial file:/tmp/iso-test/vm-serial.log -display none &
python3 .claude/skills/mjdev-qemu-test/qmp_drive.py /tmp/iso-test/qmp.sock   # writes /tmp/iso-test/*.png
```
`qmp_drive.py` screendumps boot frames, then moves the pointer to the **bottom edge** (reveals the
bottom bar) and the **right edge** (reveals the control center) and dumps each — then quits the VM.
PPM→PNG is built in (no deps). Read the PNGs with the Read tool. `var=10` ≈ black; higher ≈ content.

## Grab the session log over serial (when screenshot is black)
The serial getty prompt timing is fragile — wait ~80s, send a wake newline, then login + cat:
```sh
{ sleep 80; printf '\n\nmjdev\n'; sleep 5; \
  printf 'cat ~/.cache/mjdev-desktop/session.log.1; echo ---; cat ~/.cache/mjdev-desktop/session.log\n'; sleep 8; } \
| timeout 130 qemu-system-x86_64 -enable-kvm -cpu host -m 4096 -smp 4 \
  -kernel /tmp/iso-test/vmlinuz -initrd /tmp/iso-test/initrd.img -append "boot=live console=ttyS0,115200" \
  -cdrom "$ISO" -nographic -serial mon:stdio 2>&1 | tr -d '\r' | sed 's/\x1b\[[0-9;?]*[a-zA-Z]//g'
```

## Iterate WITHOUT rebuilding the ISO
Once an ISO with the runtime deps exists, debug session/compositor changes live:
- Share the project into the guest: add `-virtfs local,path=$PWD,mount_tag=host,security_model=none,readonly=on`
  and in the guest `mkdir -p /mnt/host && mount -t 9p -o trans=virtio host /mnt/host`.
- `mjdev` is in group `video` → it can get a seatd seat and run the compositor **without root**.
  Copy a freshly-built `mjdevc`/`mjdev-session` from `/mnt/host/...` into `~/bin`, point `MJDEVC` at it,
  run it, capture via QMP. No rebuild.
- New apt deps need root: a debug ISO can `passwd -d root` in provisioning so serial root login works.
- Only `apt`/dep changes or kernel/initrd changes truly need a rebuild — almost never.

## Reflect fixes back into the project (do this every time something works)
- runtime deps → `make-iso.sh` provision (`apt-get install ...`) **and** the deb `Depends`
  (deb packaging) so a clean console install pulls them via apt.
- session/env fixes → `session/mjdev-session`.
- compositor fixes → `compositor/native/shim.c` / `compositor/src/...` (do NOT touch the desktop app code).

## House rules (from the user)
- Don't touch the existing desktop **app** code — only compositor, session, deb packaging are fair game.
- Pure wayland (no xorg); XWayland-shim for the AWT shell is acceptable.
- Builds are heavy (mksquashfs xz pegs all cores) — if the user is in a meeting / wants quiet, kill
  `qemu-system`, `mksquashfs` (root, via pkexec), and the gradle daemon.
- `makeIso` escalates via pkexec; the script tees to `/tmp/mjdev-iso-build.log` (pkexec hides stdio).
