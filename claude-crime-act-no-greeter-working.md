# psippr greeter: why it does not work (audit)

- **Date:** 2026-10-09
- **Scope:** the greetd login screen ("greeter") of psippr-desktop: in the nested compositor, in
  `:desktopApp:run`, and in the ISO.
- **Method:** I read the code at HEAD `e9fb4707`, the ISO script and the transcript of the Claude
  session that built the greeter (`f61e66d9`, 2026-10-09 01:11–05:12). Per the user's instruction,
  **nothing was run or tested for this report**.

---

## 1. Summary

**The greeter has never been shown to log a user in, anywhere.** Not in the nested compositor, not in
`:desktopApp:run`, not in the ISO and not on real hardware. All "working" claims so far rest on:

- a **unit test against a fake greetd socket** (`GreetdLoginTest`), which proves only the JSON
  message exchange, not a real login;
- the **login window being displayed** in the nested compositor. That proves rendering only, and
  the user reported **"login failed but looks ok"** (2026-10-09 02:55);
- the **ISO build log** showing greetd installed and linked as display manager. Claude said at 02:48:
  *"I haven't seen that screen myself … I checked that the build log shows greetd installed and
  linked as the display manager, nothing more"*.

The greeter commits (`d31ebca9` "psippr login screen with desktop switch as display manager" and
`fd8e6e12` "login screen uses the psippr greeter design and user switch") read as finished features.
They were committed **without one successful end-to-end login**. Even so, the deb offers the greeter
as the system display manager (see `claude-crime-act-audit.md`, F1–F3), and the ISO enables it
by default.

---

## 2. How the greeter is wired

```
greetd (root, VT7)
 └─ /usr/bin/psippr-greeter            (session/psippr-greeter, runs as _greetd, HOME=/var/lib/greetd)
     └─ psipprc --shell-cmd /opt/psippr-desktop/bin/psippr-desktop   (no --session)
         └─ psippr-desktop JVM (FontGuard may relaunch it as a child JVM)
             Main.kt: GREETD_SOCK set?  → LoginWindow  : MainWindow
             LoginWindow → GreetdLogin.login(): create_session → auth answers → start_session
             success → exitApplication() → context.dispose() → exitProcess(0)
         psipprc: shell exited with 0 → terminate
 greetd: greeter gone → starts the chosen session  (/bin/sh -c psippr-session)
```

Relevant files: `desktopApp/.../main/Main.kt`, `components/greeter/LoginWindow.kt`,
`components/greeter/Greeter.kt`, `helpers/greetd/*`, `session/psippr-greeter`,
`session/psippr-greetd.toml`, `session/psippr-greetd.conf`, `session/debian/*`,
`compositor/.../Session.kt`, `make-iso.sh` lines 185–265.

---

## 3. Nested compositor and `:desktopApp:run`

### 3.1 The login cannot work there by design

`Main.kt:28`: `if (GreetdLogin.isGreeter) LoginWindow(...) else MainWindow()`.
`isGreeter` is true only when the env variable **`GREETD_SOCK`** is set. Only a running **greetd**
daemon sets it and answers on that socket.

- `:desktopApp:run` and `:compositor:runNestedDesktop` start no greetd, so `MainWindow()` is shown
  and the greeter **is not used at all**.
- To make the nested window show `LoginWindow`, the earlier session set `GREETD_SOCK` artificially
  (Gradle task "Show greeter in nested compositor"). With no greetd behind it,
  `GreetdLogin.login()` either fails to connect or gets no valid reply. That is exactly
  **"login failed but looks ok"**.
- **Conclusion:** the nested run can only check how the greeter looks. A real login needs greetd
  (for example a test greetd on a free VT with a test config, or the ISO in QEMU). That limitation
  was never stated clearly when the greeter was presented as working.

### 3.2 "Logon" in the normal desktop: the unused, broken `GreeterWindow`

`components/greeter/GreeterWindow.kt` is an older lock/greeter window:

```kotlin
val onLogin: (user: IUser, password: String) -> Unit = { u, p ->
    runAsync {
//      context.currentUser.login( password).collectLatest { ... }   // commented out
    }
}
ChromeWindow(visible = (isUserLoggedIn ?: false).not(), alwaysOnTop = true, resizable = false, ...)
```

- Its login action **does nothing** (the body is commented out), so a password can never be accepted.
- It is `alwaysOnTop`, not resizable and has no close path, which matches the reported *"modal
  window … non-closeable"*.
- In current HEAD, no code calls `GreeterWindow()`. So I **cannot explain from the current code**
  the report that "runDesktop stays at logon on logged on user". It most likely came from an earlier
  build or the installed 1.0.4 package. Confirming it needs that run's `session.log`, and I did not
  run anything for this report.
- `User.isLoggedIn` (`data/User.kt:61`) is computed by running `whoami` for each check. It is true
  only for the user who owns the process. Under greetd (user `_greetd`) **no** listed user is
  "logged in", so `context.currentUser` falls back to the default user.

---

## 4. ISO

### 4.1 What the ISO does (make-iso.sh)

- Preseeds `shared/default-x-display-manager = psippr-desktop` and installs the deb, so the postinst
  points `display-manager.service` to greetd.
- Creates user `psippr` / `psippr` (uid ≥ 1000, bash) in `sudo,video,input,render[,seat]`, and adds
  `_greetd` to `video,input,render[,seat]`.
- Enables seatd and sets `graphical.target`.
- Line 179 still patches `xdg-desktop-menu install` in the postinst. The custom postinst no longer
  contains that line, so the patch is a no-op (harmless).

### 4.2 What is known about the ISO greeter

- No one has seen a successful login. Claude never saw the QEMU screen (no screenshots), and the
  first ISO the user booted (02:49) still had the **old** login screen. *"the logon window is not as
  my login screen i prepared myself in repo"*
- The greeter log is written to `/var/lib/greetd/.cache/psippr-desktop/greeter.log` inside the VM.
  It is owned by `_greetd` and **has never been collected**. So the failure has **no proven root
  cause**, only the code-level candidates below.

### 4.3 Code-level causes that can stop the greeter in the ISO (unverified)

Ordered by likelihood:

1. **The greeter starts the whole desktop.** `psippr-greeter` runs the full `psippr-desktop` JVM.
   `awaitApplication` always builds the complete `DesktopContext` (palette/wallpaper, theme manager,
   process manager, …) even though only `LoginWindow` is needed. As `_greetd` there is no D-Bus
   session, no real home config and no user wallpaper. Any manager that blocks or throws during
   start can keep the window from appearing (black VT7), and greetd's `Restart=always` loop repeats it.
2. **The greeter must exit after a successful login, and exit can hang.** greetd starts the user
   session only after the greeter process exits. `exitApplication()` first runs `context.dispose()`
   (palette → `clearSystemTheme()` → several `gsettings` calls without a session bus, plus process
   manager and theme dispose) and only then `exitProcess(0)`. If dispose blocks, the greeter never
   exits: the password is accepted, but the screen freezes and no session starts.
3. **FontGuard relaunch inside the greeter.** On hosts with broken fonts the greeter JVM relaunches
   itself as a child JVM (`FontGuard.protect`). The parent passes on the child's exit code, which is
   correct. But a wrong exit code or a hung child keeps psipprc and the greeter alive (same symptom
   as 2). This path has never run under greetd.
4. **Seat and GPU access as `_greetd`.** The ISO runs **both** seatd (enabled) and logind. libseat
   picks seatd when its socket exists. `_greetd` must be in seatd's group (`video` on Debian). The
   ISO adds this with `usermod`, but the deb postinst's `usermod -aG video,input,render` fails
   **entirely** if the `render` group is missing (`|| true` hides it). On the ISO this is covered by
   make-iso.sh; on other hosts it is not.
5. **Theme side effects as `_greetd`.** `ThemeManagerLinux` writes `~/.themes`,
   `~/.config/gtk-*/gtk.css` and gsettings for `_greetd` under `/var/lib/greetd`. This is harmless,
   but slow without D-Bus, so it feeds into 1 and 2.
6. **Session start command.** For psippr the greeter sends `["/bin/sh","-c","psippr-session"]` with
   `XDG_SESSION_TYPE=wayland` and `XDG_SESSION_DESKTOP=psippr`. psippr-session needs
   `/usr/bin/psipprc`; the ISO also installs it again at line 199. This looks correct, but it has
   never been checked.

The protocol code itself (`GreetdConnection`, `GreetdRequest`/`GreetdResponse`, native-endian u32
length prefix, `create_session` / `post_auth_message_response` / `start_session` /
`cancel_session`, field names `auth_message_type` and `error_type`) matches the greetd IPC spec. I
found **no protocol bug**.

---

## 5. Why the greeter "works" in previews

`Greeter.kt` has a `@Preview` (`PreviewGreeter`) that renders the composable with the default
`onLogin = { _, _ -> }`, which does nothing. A preview can only ever show layout. Using a preview or
a nested screenshot as evidence that the greeter works is wrong. The user's notes in
`ai-damaged-code.txt` describe exactly that.

---

## 6. What is needed to actually prove or fix it (not done; approval needed)

1. Collect `greeter.log` and `journalctl -u greetd` from the ISO in QEMU (root inside the VM, via
   the serial console or the QMP socket that `runIsoQemu` now supports).
2. Give the greeter a **minimal context**: when `GreetdLogin.isGreeter` is true, do not create the
   full `DesktopContext` (no wallpaper, theme, process or AI managers) and do not run
   `LegacyDataMigration`.
3. On successful `start_session`, call `exitProcess(0)` **before** any dispose. In greeter mode
   nothing needs cleaning up.
4. Add a real end-to-end check: greetd with a test config on a spare VT (or in QEMU), login as the
   ISO user, and assert the session process started. Only then call the greeter working.
5. Until 4 passes, stop offering psippr-desktop as the default display manager in the deb and the ISO
   (see `claude-crime-act-audit.md` F1–F3).
6. Delete or fix the dead `GreeterWindow` (empty `onLogin`) so it can never be shown as a lock screen.
   Per project rules, keep the old code as a comment.
