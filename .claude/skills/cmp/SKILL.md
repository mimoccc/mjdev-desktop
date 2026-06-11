---
name: cmp
description: Compose Multiplatform / Compose Desktop (CMP) guidance for this repo — window system (ChromeWindow/DesktopWindow), DesktopProvider/DesktopScope DI, theming pipeline, AWT specifics. Use when adding desktop UI components, windows, panels, control center pages, or touching the theme/palette pipeline.
---

# Compose Desktop in mjdev-desktop

## Architecture
- Entry: `src/main/kotlin/eu/mjdev/desktop/Main.kt` → `application { MainWindow() }`.
  `Main.setAwtWindowClass()` sets X11 WM_CLASS to `mjdev-desktop` via reflection
  (needs `--add-opens java.desktop/sun.awt.X11=ALL-UNNAMED`, already in build.gradle.kts).
- DI: `DesktopProvider` (provider/DesktopProvider.kt) exposed via `LocalDesktop`;
  composables use `withDesktopScope { ... }` (DesktopScope) — `api` is the provider inside.
- Managers live in `managers/` (apps, theme, window, dbus, processes...); lazily created
  on `DesktopProvider`.

## Window system (important)
- All shell windows go through `ChromeWindow` (windows/ChromeWindow.kt) — undecorated,
  transparent, custom `Window` wrapper (windows/Window.kt) with state/focus helpers.
- `ChromeWindow` sets the AWT window **title to `mjdev::<name>`** — the mjdev compositor
  uses this prefix to classify shell windows into layers (wallpaper → background,
  others → top). When creating a new shell window, always pass a meaningful `name`.
- Wallpaper window: `DesktopWindow` → `FullScreenWindow` (alwaysOnBottom, non focusable).
- Compose Desktop runs on AWT/X11 — inside the mjdev wayland compositor it runs via
  **Xwayland** (JVM has no wayland backend). Don't rely on alwaysOnTop/toBack working
  natively there; the compositor layer policy handles stacking instead.

## Theming pipeline
- Wallpaper change → `BackgroundImage.onChange` → `Palette.update()` →
  `ThemeManagerLinux.createFromPalette()`:
  - generates GTK3/GTK4 css into `~/.themes/<Mjdev|Mjdev-alt>/` (names **alternate** —
    gtk only reloads css when the theme *name* changes) and `~/.config/gtk-{3,4}.0/gtk.css`,
  - then `gsettings gtk-theme` + `color-scheme` switch makes all running GTK apps recolor live.
- Never set `GTK_THEME` env for spawned apps (`Environment.kt` removes it) — it freezes
  the theme and kills live switching.
- Compose-side colors come from `api.palette` / `api.currentUser.theme`.

## Conventions
- Compose 1.7.0-alpha03, material icons extended, coil3 for images.
- Components live in `components/<feature>/`; control center pages in
  `components/controlcenter/pages/` (see `MainSettingsPage.kt` as template).
- Window listing/control of *other* apps: `api.windowsManager` (IPC client to the mjdev
  compositor socket `$XDG_RUNTIME_DIR/mjdev-compositor.sock`); empty fallback outside
  the mjdev session — UI must handle empty window lists gracefully.
- Run dev: `./gradlew runDesktop`; packaging: `./gradlew createPackages` (deb/appimage/rpm).
