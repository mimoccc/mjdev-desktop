# Data deletion by Claude in psippr-desktop: audit

- **Date:** 2026-10-09
- **Question:** did Claude delete any data without approval, and is any of it irreversible?
- **Sources:**
  - every Claude Code transcript stored for this project: `~/.claude/projects/-home-mimo-Plocha-mjdev-desktop/`
    (8 sessions, 2026-09-28 → 2026-10-09 05:12, under the old project name) and
    `~/.claude/projects/-home-mimo-Plocha-psippr-desktop/` (this session);
  - git history since 2026-09-28;
  - the current state of the user's home directories.
- **Method:** I scanned every Bash command for `rm`, `git rm`, `git checkout --`, `git restore`,
  `git reset --hard`, `git clean`, `git stash`, forced push, `branch -D`, `find -delete` and
  `truncate`. For each hit I read the user message before it, Claude's explanation and the
  command output.
- **Limit:** Write/Edit tool overwrites were not scanned one by one. They are in git-tracked files
  and can be recovered from history.

---

## 1. Verdict

**I found no irreversible deletion of user data by Claude in this project.**

Every deletion falls into one of these groups:

- the user explicitly requested it;
- Claude's own temporary files;
- Claude reverting its own uncommitted edits, or tool reformatting, in sessions that **started with
  a clean git tree**;
- a regenerable cache, deleted after the user approved.

Two **app behaviours** (not Claude commands) can lose or move user data. They are listed in
section 4.

---

## 2. This session (e71d5475, 2026-10-09)

| Time (UTC) | Command | What | Approval | Reversible |
|---|---|---|---|---|
| 06:11 | `rm -rf $D` | Claude's own new scratchpad dir `…/scratchpad/deb`, used to extract the deb's control files | not needed (own temp) | n/a |

Nothing else was deleted. I created three files: `claude-crime-act-audit.md`,
`claude-crime-act-no-greeter-working.md` and this file. Nothing is committed.

---

## 3. Earlier sessions

| Session | Time (UTC) | Command | What was deleted | Approval | Reversible |
|---|---|---|---|---|---|
| 23fe86d5 | 10-07 09:55 | `git rm -r .github/workflows` | `release.yml` CI workflow | **Yes**: user said *"remove github workflows"* | Yes, in git history (`73778684`) |
| 23fe86d5 | 10-07 10:10 / 12:00 / 12:26 | `rm -r shared/src/desktopTest/.../tmprender`, `tmpaccent`, `tmptext` | temporary test dirs Claude created minutes before | own temp | n/a |
| 23fe86d5 | 10-07 12:26 | `git stash push -- RemoteOpPainter.kt` … `git stash pop` | temporary stash to compare old and new code | own work | restored, *"fix restored"* confirmed in output |
| 0c95259b | 10-07 23:15 – 10-08 00:58 | `rm $T` (4×) | temporary `Zz*TmpTest.kt` files Claude created | own temp | n/a |
| d3b23194 | 10-01 21:32 | `rm -rf ~/.gradle/caches/build-cache-1` | 35 GB Gradle build cache (disk full, builds failing) | **Yes**: Claude offered options and asked; the user answered *"1"* (prune caches). Claude deleted less than offered (only `build-cache-1`; `modules-2` and the downloaded libraries were left alone) | Regenerable; it holds task outputs only |
| d3b23194 | 10-01 22:11 | `git checkout androidApp/.../themes.xml` | Claude's own edit from 22:09 | own edit; session started with a **clean** tree | n/a |
| d3b23194 | 10-01 22:16 | `git checkout .../MainView.kt`, `rm PortraitEdgeControls.kt PortraitEdgeDefaults.kt` | Claude's own edits and files from 22:03–22:08 | user asked *"take that icons out"*; clean tree at start | n/a |
| d41844a1 | 10-02 15:27 | `git checkout -- PermissonManager.kt NetDetail.kt RemoteOpPainter.kt shared/src/desktopMain/.../managers` | formatting changes that `ktlintFormat` made to unrelated files during the task | session started with a **clean** tree, and Claude had not edited `managers/` | n/a |
| 75255761 | 10-08 04:50 | `git tag -f pre-psippr-rename HEAD` | a safety tag before the rename (`-f` would move an existing tag of that name) | part of the requested *"rename mjdev-desktop safely"* | Yes |
| 75255761 | 10-08 05:24 | `git checkout pre-psippr-rename -- shared/src/commonMain/resources/vlc` + `commit --amend` | **restored** 37 VLC resource files the rename had broken. This undid a loss rather than causing one | requested rename | Verified: 0-line diff against the tag |
| 75255761 | 10-08 05:30 | `rm -rf …/scratchpad/ctl` | own temp | own temp | n/a |
| d33eeb2e, d41844a1, f61e66d9 | various | `rm -rf $S/...`, `rm -f $S/qmp/qmp.sock` | own scratchpad files and sockets | own temp | n/a |

**Residual risk:** `git checkout <file>` discards **all** uncommitted changes in that file. In the
d3b23194 and d41844a1 sessions the tree was clean when the session started and Claude's own edits
were the only known changes. If the user edited those exact files by hand during the same 15–40
minutes, those hand edits would be gone. Nothing in the transcripts shows that happened.

### Files deleted in git history (verified still present)

| Commit | Deleted paths | Where they are now |
|---|---|---|
| `40fe01d9` rename mjdev → psippr | 10 files | all 10 exist under `org/psippr/...`, `session/psippr.desktop` or `data/DecodedImage.kt` (renamed, with content changed) |
| `98fab8ea` remote compose in tabs | `RemoteTabPlugin.kt`, `RemoteDesktopManager.kt` | `RemoteTabPlugin.kt.disabled`; split into `RemoteDesktopManager.desktop.kt` / `.android.kt` |
| `73778684` remove github config | 4 `.github/*` files | removed on user request, in history |
| `1a8b0b95` code ideas | `.aider.conf.yml` | in history |

---

## 4. Data moved or lost by the **app**, not by a Claude command

1. **`~/.mjdev` was moved into `~/.psippr`** (the separate psippr project's folder). An
   uncommitted `LegacyDataMigration` did this on 2026-10-09 around 01:23 when the app was started.
   It was a move (`Files.move`), not a deletion, and the user never approved it. A later fix moved
   this app's `desktop/` and `plugins/` on to `~/.psippr-desktop/` at 03:46. Current state:
   `~/.mjdev` is gone, `~/.psippr` is **empty**, and `~/.psippr-desktop/{desktop,plugins}` holds the
   data. **Nothing was lost as far as the file system shows**, but data belonging to two projects
   was mixed for about 2 hours.
2. **`~/.config/gtk-3.0/gtk.css` and `~/.config/gtk-4.0/gtk.css` are overwritten** by the theme
   manager, with no backup, and **deleted** on clean exit (see `claude-crime-act-audit.md` F6). Both
   files on this host are currently psippr-generated. **If they held the user's own CSS before
   psippr-desktop first ran, that content is irreversibly lost.** It cannot be checked now, because
   these files are not in any backup or git.
3. gsettings (`gtk-theme`, `color-scheme`, `accent-color`) were changed; the current value is
   `gtk-theme = 'Psippr-Desktop'`. Previous values are kept only in memory, so after a crash they
   cannot be restored exactly.

---

## 5. Not covered

- The transcripts of the **other** projects (`-home-mimo-Plocha-psippr`,
  `-home-mimo-Plocha-psippr-ai-offline`) contain 362 deletion-pattern hits, mostly `rm -rf $S`
  scratchpad cleanups. They are **outside this repository and were not audited** here. That can
  be done as a separate report if wanted.
- Sessions whose transcripts no longer exist locally cannot be audited.
