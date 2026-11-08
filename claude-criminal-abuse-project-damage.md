# psippr greeter: full state after the failed boot login of 2026-10-30

- **Date:** 2026-10-30, written 04:10 CET
- **Scope:** the greetd login screen ("greeter") of psippr-desktop as installed on the host
  `mjdev-laptop` (deb 1.0.5, installed 2026-10-30 ~01:58) and its code on `main`.
- **Method:** host journal, installed files, the repository, a nested `psipprc` with a mock greetd
  (accepts only the password `test`), screenshots from the nested X server. Root-owned greeter
  logs under `/var/lib/greetd` were **not** read (no root access was used).
- Earlier report on the same subject: `claude-crime-act-no-greeter-working.md` (2026-10-09).

---

## 1. Summary

After the reboot at 02:12 the host started the psippr greeter as its display manager. Two bugs,
both introduced by Claude in commits on `main`, made it fail:

| # | Symptom on the host | Root cause | Introduced | Fixed |
|---|---|---|---|---|
| A | Correct password, then "nothing": back to the login screen | session command broken by greetd's shell joining | `c72f4df7` (2026-10-30 01:49) | `5288287e` |
| B | Session switch button missing | black GNOME logo drawn on the dark login screen | `f8794a23` (2026-10-09 09:07) | `d2e416ea` |

Both fixes are committed on `main`, **not pushed and not installed**. The host was switched back
to GDM by the user (`display-manager.service -> gdm3.service`, verified).

A real end-to-end login through the real greetd and PAM with the fixed build has **still never
been done**. The greeter must not be called "working" until it has.

---

## 2. Evidence from the host

`journalctl -b -u greetd` (boot of 2026-10-30 02:12):

```
02:12:22 greetd: session opened for user _greetd        (greeter started)
02:13:18 greetd: pam_unix(greetd:session): session opened for user mimo
02:13:19 greetd: gkr-pam: unlocked login keyring
02:13:19 greetd: pam_unix(greetd:session): session closed for user mimo
02:13:20 greetd: session opened for user _greetd        (greeter back)
```

- The password was accepted (PAM session opened, login keyring unlocked).
- The user session closed **1 second** later.
- No `~/.cache/psippr-desktop/session-*.log` exists, although `c72f4df7` was meant to create one
  for every session start.

---

## 3. Bug A: the login led to nothing

**Where:** `SessionLog.wrap()` (`helpers/greetd/SessionLog.kt`), used by `GreetdLogin.authenticate()`.

`c72f4df7` wrapped the session command as `["/bin/sh", "-c", "<script>", "psippr-session-log",
<name>, <cmd>...]` so its output would land in a log. greetd does **not** run this list as argv:
it joins the elements with spaces and runs

```
/bin/sh -c "[ -f /etc/profile ] && . /etc/profile; [ -f $HOME/.profile ] && . $HOME/.profile; exec <joined cmd>"
```

(string verified in `/usr/sbin/greetd` 0.10.3). The quoting of the script is lost, so the shell
runs `exec /bin/sh -c dir="$HOME/.cache/psippr-desktop"`, which only assigns a variable and
exits with 0. The session ends at once and no log is created. A manual reproduction of that
line exited with 0 and created no files, matching the journal.

The unit test of `c72f4df7` ran the wrapper through `ProcessBuilder` (real argv), never through
the greetd command path, so it passed while the real path was broken.

**Fix `5288287e`:** new `ShellQuote` object and `SessionLog.greetdCmd()`: every argument is POSIX
single-quoted, so greetd's joined line splits back into the exact argv. `GreetdLogin` uses it;
the old line is kept as a comment.
Regression test `SessionLogTest.sessionStartedTheGreetdWayRunsAndLogs` runs the command exactly
as greetd does (`/bin/sh -c "exec " + joined`) and checks that the session runs and logs.
Nested check: password `test` accepted, `start_session` received the quoted command, greeter
and `psipprc` exited by themselves (`psipprc exit=0`).

---

## 4. Bug B: the session switch button was missing

**Where:** `SessionSwitch` shows the logo of the preselected desktop (`SessionIcon`), found by
`LoginSessionIcons` through the file name patterns of `SessionIconName`.

- The preselected desktop is the one remembered per account (`LastLoginStore`), so the bug
  depends on the account shown: the user saw the button after changing account.
- For **Ubuntu** and **GNOME** the lookup skips Ubuntu's CSS-styled `ubuntu-logo.svg` and ends on
  `/usr/share/icons/gnome-logo-text.svg`. That file has no fill colors and renders **black**.
- The login screen background is near black (`#1e1e1e`), so the button was practically
  invisible. Rendering the logo on that color confirmed it.
- For psippr the logo is a bright PNG, so the button showed for accounts that remembered psippr.

Nothing in the code checks the user's identity to show or hide the button: `LoginWindow` always
composes `SessionSwitch`.

Not proven: which desktop was remembered for `mimo` in `/var/lib/greetd` at boot (file not read).

**Fix `d2e416ea`:** new `SessionIconName.LogoTextDarkSvg` (`-logo-text-dark.svg`) tried before
`-logo-text.svg`, so GNOME and Ubuntu get GNOME's white logo made for dark backgrounds.
Regression test `LoginSessionIconsTest.darkBackgroundLogoWinsOverPlainTextLogo`.
Nested check with "Ubuntu on Wayland" preselected: white GNOME logo visible in the bottom-right
corner.
Known side effect: inside the open menu (white surface) the GNOME / Ubuntu logos are white on
white; the desktop names stay readable.

---

## 5. Wrong statements made by Claude during the investigation

Recorded so they are not repeated:

1. "The button is there and works": it was not visible on the user's screen.
2. "The 2026-10-09 move to the corner caused it": the user saw the button after that date.
3. "The compositor places the login window at 90 %": the compositor log shows it at full size.
4. "The content is drawn 1.2x too large": the first screenshot was clipped by the host window
   frame; a capture from the nested X server showed correct size and centering.

Claude also gave the user a command that asked for the login password (to search local files
for it). Asking for a password in a generated command is what phishing looks like; it must not
be done again.

---

## 6. Password handling (verified in code, not a full audit)

- `GreetdLogin` / `GreetdConnection` send the password only to the greetd Unix socket
  (`SocketChannel` on `UnixDomainSocketAddress`). No network code in the greeter files.
- The greeter log writes replies only (`logReply`), never requests, so never the password.
- The mock greetd used in testing logs only "accepted" / "rejected".
- Not audited: the rest of the application and the host.

The user also reported printer activity. The host shows no print jobs and no CUPS requests after
`cups-browsed` refreshed the Brother queue at 02:21:04.

---

## 7. Greeter history

| Commit | Date | Change |
|---|---|---|
| `d31ebca9` | 2026-10-09 04:30 | psippr login screen as display manager |
| `fd8e6e12` | 2026-10-09 04:54 | greeter design and user switch |
| `4b0510af` | 2026-10-09 09:07 | users as avatar row |
| `f8794a23` | 2026-10-09 09:07 | session menu in the corner with session icons (bug B) |
| `f16ef0f5` | 2026-10-29 22:37 | greeter module as own app |
| `e8917561` | 2026-10-29 22:52 | greeter as own psippr-greeter deb |
| `3dc8d19d` | 2026-10-30 00:41 | greeter back inside the desktop deb |
| `3fa7d0d8` | 2026-10-30 01:46 | Gradle tasks to switch psippr / GDM |
| `c72f4df7` | 2026-10-30 01:49 | session output logging (bug A) |
| `5288287e` | 2026-10-30 02:31 | fix A |
| `d2e416ea` | 2026-10-30 04:04 | fix B |

---

## 8. Still open

1. Real login through greetd + PAM with the fixed build, on the host or the ISO in QEMU.
2. Session logos white on white inside the open menu.
3. The points of section 6 of `claude-crime-act-no-greeter-working.md` (minimal greeter context,
   exit before dispose, ISO end-to-end check) were not re-checked in this session.
4. Uncommitted formatting changes from before this session remain in `GreeterUserRow.kt`,
   `LoginWindow.kt`, `SessionSwitch.kt` and `NeighbourPages.kt`.

---

## 9. Real greetd + PAM login with the fixed build (2026-10-30 14:35)

- **Where:** the ISO `releases/psippr-desktop-1.0.5.iso` (built 2026-10-29 22:21, still the broken
  greeter) booted in QEMU; inside the VM the deb built from `main` at `16bbd88e`
  (`packageFullDeb`, contains `ShellQuote`, installed jar checksum equal to the built one) was
  installed with `dpkg -i` and `greetd` restarted. The host was not touched.
- **Login:** the default live account typed into the psippr greeter on tty7 through QMP keys.
- **Result:** `pam_unix(greetd:session): session opened for user psippr`, no "session closed";
  logind session on seat0 / tty7; running `psippr-session` -> `psipprc --session` ->
  `/opt/psippr-desktop/bin/psippr-desktop`; `session-psippr-desktop.log` records
  `start: /bin/sh -c psippr-session`; the screen shows the psippr desktop (Home icon, bottom
  bar, clock).
- Section 8 point 1 is therefore done for the ISO. Still not done: the same login on the host.
- Section 4 (bug B, logo colors) is rejected by the user as wrong; it is not a proven cause.
