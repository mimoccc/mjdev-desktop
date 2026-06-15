---
name: mjdev-privilege-escalation
description: How to run privileged (root) steps in this project on Linux — prefer a graphical pkexec dialog in a GUI session, fall back to sudo, run directly when already root (CI). Use when a task/script needs root (install, debootstrap/chroot, mount, dpkg/apt, writing under /usr) and must work both from the IDE/desktop and headless/CI without hanging on a password prompt.
---

# Privilege escalation (Linux) — pkexec in GUI, else sudo, else direct

When a step needs root, pick the escalation method by environment — never assume an interactive
terminal exists (the Gradle daemon has no TTY, so a bare `sudo` would hang).

## Decision order
1. **Already root** (CI, containers): run the command directly, no escalation.
   `id -u == 0` or `USER == root`.
2. **GUI session** (`DISPLAY` or `WAYLAND_DISPLAY` set) **and** `pkexec` available:
   use **`pkexec`** → it shows a graphical polkit authentication dialog (needs no TTY, works from
   a detached daemon). This is the preferred desktop path.
3. **Otherwise**: fall back to **`sudo`** (works with passwordless/NOPASSWD sudo on CI runners).

## Gotchas (learned)
- `pkexec` **resets the environment and detaches stdio** — the child's stdout/stderr do NOT reach
  the caller's console. If you need the output, have the privileged script `tee` to a log file
  (e.g. `exec > >(tee /tmp/x.log) 2>&1`) and read that file afterwards.
- `pkexec` hands work back as root: chown results back to the invoking user via `PKEXEC_UID`
  (or `SUDO_UID` under sudo) so files aren't left root-owned in the user's tree.
- A polkit **agent must be running** in the session for the dialog to appear (true on GNOME/KDE).
- Reference implementations in this repo: `makeIso` and `installDesktop` (build.gradle.kts /
  compositor.gradle.kts) choose root/pkexec/sudo exactly this way.

## Kotlin/Gradle snippet (the pattern)
```kotlin
val args = mutableListOf<String>()
val isRoot = System.getenv("USER") == "root" ||
    runCatching { ProcessBuilder("id","-u").start().inputStream.bufferedReader().readText().trim() == "0" }.getOrDefault(false)
when {
    isRoot -> {}                                                            // direct
    File("/usr/bin/pkexec").canExecute() && !System.getenv("DISPLAY").isNullOrBlank() -> args += "pkexec"
    else -> args += "sudo"
}
args += listOf("/bin/bash", scriptPath, /* … */)
ProcessBuilder(args).inheritIO().start().waitFor()
```

## Shell snippet
```sh
if [ "$(id -u)" -eq 0 ]; then SUDO=;
elif command -v pkexec >/dev/null && { [ -n "$DISPLAY" ] || [ -n "$WAYLAND_DISPLAY" ]; }; then SUDO=pkexec;
else SUDO=sudo; fi
"$SUDO" sh -c '…privileged…'
```
