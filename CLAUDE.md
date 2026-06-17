# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

mjdev-desktop is a Kotlin Compose Multiplatform desktop environment targeting Linux (primary), Android, and Windows. It has two runtime layers:

1. **Kotlin/JVM shell** (`shared` + `desktopApp`) — the full desktop UI built with Jetpack Compose Desktop. This is the primary development surface.
2. **Native Wayland compositor** (`compositor`) — a Kotlin/Native binary (`mjdevc`) wrapping wlroots via a thin C shim. Only handles seat/input/focus/surface layering — all desktop policy lives in the Kotlin shell.

## Gradle Commands

```bash
# Run desktop app (JVM, no compositor needed)
./gradlew :desktopApp:run

# Run desktop inside a nested Wayland compositor (needs an X11/Wayland host session)
./gradlew :compositor:runNestedDesktop

# Build all distributables (deb, rpm, AppImage, apk) → releases/
./gradlew buildAll

# Build and install the full desktop (deb + compositor + session) via pkexec dialog
./gradlew :compositor:installDesktop

# Build desktop deb only
./gradlew :desktopApp:packageReleaseDeb

# Lint (report only — never breaks the build)
./gradlew ktlintCheck

# Auto-format
./gradlew :shared:ktlintFormat

# Dependency update report → reports/dependencies/
./gradlew :shared:dependencyUpdates

# Build ISO (needs debootstrap / squashfs-tools / xorriso / grub tools + root/pkexec)
./gradlew makeIso

# Run ISO in QEMU headlessly (no root needed)
./gradlew runIsoQemu
# or directly:
./run-iso-qemu.sh

# Android APK
./gradlew :androidApp:assembleRelease
```

`ktlintFormat` + `ktlintCheck` run automatically after every `build` task via the `postBuildCodeCheck` hook.

## Module Structure

| Module | Language | Purpose |
|---|---|---|
| `shared` | Kotlin Multiplatform (JVM + Android) | All UI components, managers, business logic |
| `desktopApp` | Kotlin JVM | Entry point (`org.mjdev.desktop.main.MainKt`), compose desktop wiring |
| `androidApp` | Kotlin Android | Entry point for Android target |
| `compositor` | Kotlin/Native (linuxX64) + C | Wayland compositor binary `mjdevc` |
| `buildSrc` | Kotlin DSL | Custom Gradle tasks: `PackageFullDebTask`, `PackageAppImageTask`, `EnsureAppImageToolTask`, `AiAgentPlugin` |

Build file naming convention: each module uses `<name>.gradle.kts` (not `build.gradle.kts`).

## Architecture

### Desktop Context (`IDesktopContext`)

`IDesktopContext` is the central dependency injection hub. It lives in `shared/src/commonMain` and is provided to the Compose tree via `LocalDesktopContext`. Every composable accesses it through `DesktopContextScope.withDesktopContext { ... }` which gives access to all managers, theme, palette, user, and image loader.

The concrete desktop implementation is `DesktopContext` in `shared/src/desktopMain`. The Android implementation is in `shared/src/androidMain`.

### Managers

All managers are interface-based (`IAppsManager`, `IAiManager`, `IPalette`, etc.) and lazily instantiated via `ManagerCache` using Kotlin property delegation (`by this` on `IDesktopContext`). Managers live under `shared/src/commonMain/.../managers/`:

- `ai/` — AI integration (Gemini, OpenAI plugins, STT/TTS)
- `apps/` — `.desktop` file parsing and app catalogue
- `connectivity/` — network state
- `keys/` — keyboard shortcuts
- `palette/` — dynamic color extraction from wallpaper
- `process/` — launching processes
- `theme/` — theme management (GTK theme auto-generation)
- `translations/` — i18n

### UI Components

All shared UI lives in `shared/src/commonMain/.../components/`. Platform-specific overrides (e.g. window management) are in `desktopMain` and `androidMain`. Key components:

- `desktop/` — the root desktop surface
- `appbar/` — top/bottom bars
- `appsmenu/` — application launcher
- `controlcenter/` — settings panel (pages wired via `IDesktopContext.controlCenterPages`)
- `background/` — animated wallpaper with crossfade and queue
- `desktoppanel/` — configurable desktop panel
- `greeter/` — login/greeter screen
- `window/` — window chrome and management (desktop-only)

### KMP Source Sets

- `commonMain` — shared UI + all interfaces and manager contracts
- `desktopMain` — JVM/AWT specifics: window management, `DesktopContext`, platform extensions
- `androidMain` — Android-specific context and extensions
- `nativeInterop` — cinterop definitions for the compositor shim

### Compositor (`compositor/`)

The `mjdevc` binary is a **Kotlin/Native** executable. It:
1. Wraps `wlroots-0.18` through `compositor/native/shim.c` (a flat C API compiled to `libmjcshim.a`)
2. The Kotlin side (`compositor/src/linuxX64Main/`) receives view lifecycle callbacks (`WindowModel`, `Policy`, `GeometryStore`), manages IPC to the Kotlin shell (`Ipc.kt`), and handles session startup (`Session.kt`)
3. The compositor launches the Kotlin desktop shell as a subprocess (`--shell-cmd`)

**Kotlin-first rule**: Any desktop behavior (autohide, menus, layout, state, UX) must be implemented in the Kotlin shell. Only add C code for genuine Wayland compositor concerns: seat/keyboard/pointer focus of wlr surfaces, surface layer ordering, output/mode handling.

A behavior must work in plain `./gradlew :desktopApp:run` (JVM, no compositor) first.

### Session & Installation

The Wayland session entry is `session/mjdev.desktop` (installed to `/usr/share/wayland-sessions/`). The session launcher is `session/mjdev-session` (shell script). The compositor runtime requires: `libwlroots-0.18 xwayland libegl1 libgles2 libgbm1 libinput10 libseat1 libxkbcommon0 libgl1-mesa-dri dbus dbus-user-session` — single source of truth in `gradle/libs.versions.toml` under `app-compositor-runtime-deps`.

Session logs go to `~/.cache/mjdev-desktop/session.log`.

### Packaging & Release

`./gradlew buildAll` produces:
- `releases/mjdev-desktop-<version>.deb` — full self-installable deb (includes compositor + session + wayland runtime `Depends`)
- `releases/mjdev-desktop-<version>.AppImage`
- `releases/mjdev-desktop-<version>.rpm`
- `releases/mjdev-desktop-<version>.apk`
- `releases/mjdev-desktop-<version>.iso` (if ISO tools present)

Output `packages/` directory structure: `packages/main-release/{deb,rpm,appimage}/` and `packages/main/app/` (distributable).

## Code Style

- **ktlint** is configured project-wide; `ktlint_standard_no-unused-imports = disabled` (see `.editorconfig`) because receiver/extension imports are used and would be falsely stripped.
- **No hardcoding** — all app metadata (version, name, maintainer, runtime deps) is in `gradle/libs.versions.toml`. Never hardcode these values in build scripts.
- **One component/class per file** — each Kotlin file contains exactly one top-level class or composable.
- **Constants**: use `enum` for grouped constants; use `companion object` only for single-file constants.

## Commit Conventions

Format: `<type>(<scope>): <description>` — types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`. Description under 50 characters, imperative mood, lowercase.

Branch naming: `<type>/<issue-number>-<short-description>` — types: `feature`, `bugfix`, `hotfix`, `release`, `support`.
