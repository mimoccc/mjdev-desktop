---
name: mjdev-package-dependencies
description: Multiplatform packaging rule for mjdev — every distributable that has runtime dependencies must declare them through that package format's native dependency mechanism (deb Depends, rpm Requires, …) so the OS package manager resolves them on install; if the format has no dependency system, bundle the dependencies inside the package. Use when building/reviewing deb/rpm/AppImage/exe/msi/dmg/flatpak/apk packaging so a clean install "just works" on any machine.
---

# Packaging: declare deps natively, else bundle them

Goal: a user who **gets a package and installs it must be able to use the app on any machine** —
the package pulls or carries everything it needs. Never ship a package that silently misses a
runtime dependency (that is what black-screened fresh installs).

## The rule
For each distributable, put runtime dependencies where that format resolves them:

| Format     | Dependency mechanism (preferred)                     | If not possible → bundle |
|------------|------------------------------------------------------|--------------------------|
| **.deb**   | `Depends:` (+ `Recommends:`) in `DEBIAN/control` → apt resolves | ship the lib in the package + ldconfig/rpath |
| **.rpm**   | `Requires:` tags → dnf/zypper resolve                | ship the lib in the package |
| **AppImage** | none — **bundle everything** in the AppDir (that's the point of AppImage) | (always bundle) |
| **.exe/.msi** | installer prerequisites / bundled redistributables | bundle DLLs next to the exe |
| **.dmg/.pkg** | macOS frameworks / `@rpath`                        | bundle dylibs in the .app |
| **flatpak** | runtime + extensions in the manifest                | bundle in the sandbox |
| **.apk**   | gradle deps compiled in (self-contained)             | (always self-contained) |

Prefer the native dependency system (smaller package, shared/updated libs). Only **bundle** when
the format has no resolver (AppImage) or the dep is unavailable / ABI-pinned on the target distro.

## Single source of truth (no hardcode — see [[mjdev-code-conventions]])
Keep the dependency list in **one place** (the version catalog: `app-compositor-runtime-deps`) and
feed every packager + installer + ISO from it. Don't copy the list into each packaging step.

## TWO product shapes — don't conflate them
1. **Full desktop session** = deb / rpm / live ISO. Ships the compositor (`mjdevc`) + `mjdev-session`
   + the `wayland-sessions` entry, and the app runs as the session **shell**. THESE need the wayland
   runtime stack (libwlroots/xwayland/mesa/seatd) declared/bundled — a clean/console box has no
   compositor. The desktop session is **Linux-only**.
2. **Standalone app** = AppImage / exe / msi / dmg / apk. The app runs as a **normal window on the
   user's EXISTING desktop** (GNOME/KDE/other wayland or X) — that environment already provides a
   compositor. So **do NOT bundle mjdevc / wlroots here**; just ship the self-contained app (jpackage
   already bundles the JRE + Skiko). On a real desktop GPU, Skiko uses hardware GL normally.

## This project's reference
- deb (shape 1): `PackageFullDebTask` (buildSrc) injects compositor + session and appends the wayland
  runtime stack to `Depends` (from the catalog), so `apt install ./mjdev-desktop.deb` pulls
  libwlroots/xwayland/mesa/seatd on a clean console Linux.
- rpm (shape 1): same completeness via `Requires:` (rpm rebuild) — TODO.
- AppImage/exe/dmg/apk (shape 2): leave as jpackage produces — self-contained app, **no compositor**.
- `buildAll` (and the GitHub Actions release) must emit ALL formats — the more platforms the better.
