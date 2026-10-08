---
name: psippr-runtime-modes
description: How the psippr desktop actually runs - plain JVM app, nested ("boxed") wlroots compositor, or the real psippr Wayland session - plus the compositor IPC/RPC socket, the font-guard parent/child JVMs and how to test / stop / debug a run safely. Apply before running, testing, debugging hangs, or reasoning about which environment (XWayland, GNOME, psipprc) a behavior happens in.
---

# psippr runtime modes

## The three ways the shell runs
| Mode | Start | Display server | Compositor IPC socket | `XDG_CURRENT_DESKTOP` |
|---|---|---|---|---|
| Plain app (dev) | `./gradlew :desktopApp:run` | host session (GNOME: XWayland) | none | host value (e.g. `ubuntu:GNOME`) |
| Nested / boxed | `./gradlew :compositor:runNestedDesktop` | `psipprc` wlroots compositor inside a host X11 window (`WLR_BACKENDS=x11`, `PSIPPRC_OUTPUT=1280x720`); the shell runs as its client via XWayland | yes | `psippr` |
| Real session | display manager -> `session/psippr-session` -> `psipprc --shell-cmd /opt/psippr-desktop/bin/psippr-desktop` | `psipprc` on the hardware | yes | `psippr` |

- `psipprc` (Kotlin/Native, `compositor/`) sets `XDG_CURRENT_DESKTOP=psippr`, `XDG_SESSION_TYPE=wayland`, spawns xdg-desktop-portal(-gtk) and the shell.
- Code that must behave differently per mode reads `XDG_CURRENT_DESKTOP` (see `managers/theme/linux/session/DesktopSession`): e.g. hiding client titlebuttons only when the compositor draws frames (PSIPPR), GNOME Shell theming only under GNOME.
- A behavior must work in the plain app first (see `psippr-kotlin-first`).

## Compositor IPC ("RPC")
- Unix socket `$XDG_RUNTIME_DIR/psippr-compositor.sock` (`compositor/.../IpcServer.kt`, path from `Main.kt`), line based JSON.
- Requests: `list-windows`, `windows-for-app`, `activate`, `close`, `minimize`, `maximize`, `subscribe`, `pointer-move`, `button`, `click`, `key`, `move`, `blur`, `blur-config`.
- Events to subscribers: `window-opened`, `window-closed`, `window-title`, `focus-changed`, pointer broadcasts.
- Shell side: `helpers/mouseevents/CompositorMouseSource` connects per window; in the plain app it logs `no compositor socket … not connecting` - so IPC is NOT involved in plain-app runs.

## Font guard: two JVMs per run
`FontGuardRelauncher` re-launches the app in a child JVM with `FONTCONFIG_FILE=~/.cache/psippr-desktop/fonts-guard.conf` (hides family-less fonts that freeze Skia `FontMgr`). A run therefore has: gradle wrapper -> parent JVM -> child JVM (the real UI). The parent's shutdown hook only `destroy()`s (SIGTERM) the child.

## Testing / stopping a run safely
- Tell the user before a run opens windows; ask them not to touch them. A closed window ends the run early ("Exiting application" in the log) and invalidates the test.
- The real UI process is the child: `pgrep -f "compose.application.resources.dir=<worktree>/"` -> highest pid.
- Thread dump: `jcmd <child> Thread.print -l`. Stop: `kill -TERM <child>`; if still alive after ~20 s, `kill -KILL`.
- Never `pkill -f` a pattern that also matches your own shell command line.
- Runs with the live theme switch change the user's GNOME settings (gtk-theme, accent, color-scheme, User Themes name); a killed run cannot restore them - restore by hand afterwards.
- Compare against an older commit in a scratch `git worktree` instead of touching the user's tree.

## Known: intermittent hang on SIGTERM (2026-10-08, plain app under GNOME/XWayland)
The UI thread holds the AWT lock while blocked in skiko native `LinuxOpenGLRedrawer.setSwapInterval`; `AWT-XAWT` and `XToolkt-Shutdown-Thread` wait for that lock, so shutdown never finishes and only SIGKILL works. Seen 2 of 3 runs with the live theme switch, 0 of 7 without; external gsettings changes did not reproduce it.

- The stuck call is skiko `LinuxOpenGLRedrawer.kt:147` = `setSwapInterval(0)`, the path every non-vsync window takes (all desktop windows but one). Disabling vsync (`skiko.vsync.enabled=false`) sends ALL windows through it - do not use it as a fix.
- Mitigation in place: `FontGuardRelauncher` stops the child via `ChildProcessStopper` (SIGTERM, 5 s grace, then SIGKILL), so a hung child no longer survives as an unstoppable orphan.
