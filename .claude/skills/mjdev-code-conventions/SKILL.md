---
name: mjdev-code-conventions
description: Mandatory mjdev project coding conventions — apply when writing, reviewing, or refactoring ANY code in this repo (Kotlin, Compose, Gradle, build scripts). Covers no-hardcoding, one-component/one-class-per-file, and the enum vs companion-object rule for constants.
---

# mjdev project code conventions (non-negotiable)

Apply these to every change in this repository. The user enforces them strictly.

## 1. No hardcoding
Never inline literal values that belong in configuration. Read them from the **version catalog**
(`gradle/libs.versions.toml`) or the appropriate config, and thread that single source through
build scripts, tasks, and shell helpers.
- Versions, app metadata, package names, dependency lists, paths, URLs → catalog / config.
- Example precedent: the wayland runtime dependency list lives once as
  `app-compositor-runtime-deps` in the catalog and is consumed by the deb packaging task,
  `installDesktop`, and `make-iso.sh` (which reads it via its `read_catalog` sed) — not copied.
- Shell scripts that can't parse TOML read catalog keys with the existing `read_catalog` helper.
- If a value truly has no config home, justify it; default is: put it in config.

## 2. One Compose component per file
Each `@Composable` UI component gets its own file named after it. No bundling multiple
components into one file. Previews for that component may live in the same file.

## 3. One class per file
Each class/interface/object gets its own file named after it. No multi-class files.

## 4. Constants: enum first, else companion object
- Group related constants as an **`enum class`** (or sealed type) — preferred.
- If an enum doesn't fit (e.g. unrelated scalar constants for one class), put them in a
  **`companion object`** of the owning class. Never scatter loose top-level `const val`s or
  magic literals through the code.

## Scope note for this repo
The user has said the **desktop app code is off-limits** during the ISO/compositor/packaging work —
only `compositor/`, `session/`, and deb/ISO packaging are fair game there. These conventions still
apply to whatever code you DO touch. Related: [[no-hardcode]] memory.
