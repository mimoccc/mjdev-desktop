---
name: kmp
description: Kotlin Multiplatform (KMP) guidance for this repo — Kotlin/Native compositor module, cinterop with C libraries, posix API usage, Gradle multiplatform setup. Use when working on the compositor/ module, cinterop defs, K/N memory management, or adding KMP targets.
---

# Kotlin Multiplatform in mjdev-desktop

## Project layout
- Root module: Kotlin **JVM** (Compose Desktop app, `src/main/kotlin`), Kotlin 1.9.22, JVM toolchain 21.
- `compositor/`: Kotlin **/Native** `linuxX64` module producing the `mjdevc` wayland compositor binary.
  - C shim: `compositor/native/shim.{h,c}` over wlroots 0.18; `shim.h` is **self contained**
    (no wlroots includes) so cinterop can parse it directly.
  - cinterop def: `compositor/native/shim.def`, package `mjdev.compositor.shim`.
  - The shim compiles separately (`:compositor:compileShim` → `libmjcshim.a`) and is linked
    via `linkerOpts`; wlroots flags come from `pkg-config` resolved lazily (never fails configuration).

## Gradle gotchas (this repo)
- **Do not use `alias(libs.plugins...)` in subproject `plugins {}` blocks** — the buildSrc project
  breaks generated catalog accessors there. Use `kotlin("multiplatform")` without version
  (inherited from root classpath). For versions/values use the catalog API:
  `extensions.getByType<VersionCatalogsExtension>().named("libs").findVersion(...)`.
- Root `build.gradle.kts` keeps `repositories` in `allprojects` but `dependencies` **root-only** —
  JVM/Compose deps must never leak into the K/N module.
- K/N source set name: `linuxX64Main` (`compositor/src/linuxX64Main/kotlin`).

## Kotlin/Native patterns used
- C callbacks: flat struct of function pointers (`mjc_callbacks`), filled with
  `staticCFunction(::topLevelFun)`; context passed via `StableRef<Compositor>.asCPointer()`
  and recovered with `asStableRef<T>().get()`. staticCFunction cannot capture state.
- Strings: `CPointer<ByteVar>?.toKString()`; Kotlin `String` auto-converts to `const char*` params.
- Sockets/IO in compositor: raw `platform.posix` (socket/bind/listen/accept/read/write,
  `O_NONBLOCK` via fcntl), integrated into the wayland event loop through `mjc_loop_add_fd` —
  no extra threads, the wayland server is single threaded.
- JSON: `kotlinx-serialization-json` (works on native targets).
- Never call shim functions from other threads; everything runs on the wl_event_loop thread.

## Build & run
- `./gradlew :compositor:compileKotlinLinuxX64` — Kotlin compile + cinterop (no wlroots needed).
- `./gradlew :compositor:linkMjdevcDebugExecutableLinuxX64` — needs apt: libwlroots-0.18-dev
  libwayland-dev wayland-protocols libxkbcommon-dev libinput-dev libpixman-1-dev libwayland-bin.
- `./gradlew :compositor:runNested` — runs compositor as a window inside the current session.
