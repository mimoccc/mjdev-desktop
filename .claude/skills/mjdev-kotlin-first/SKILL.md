---
name: mjdev-kotlin-first
description: Kotlin-first implementation rule for mjdev — solve every behavior in Kotlin/JVM (the shared desktop shell) whatever it costs; only reach into the C compositor (compositor/native/shim.c) when the behavior is genuinely a compositor concern (seat/input/focus/surface layering) or when nothing else can possibly work. Apply when deciding WHERE a feature or fix belongs, and when reviewing changes that touch native compositor code.
---

# mjdev Kotlin-first rule (non-negotiable)

The desktop is **Kotlin-first**. Default to solving everything in the Kotlin/JVM shell — the
shared Compose Desktop code — no matter how awkward or expensive it is there. The C compositor
(`compositor/native/shim.c`, wlroots) is a last resort.

## Decision order
1. **Can it be done in Kotlin/JVM?** → Do it there. This is the answer in the vast majority of
   cases (autohide, reveal hotspots, menu coupling, window geometry, timeouts, state, UX logic).
   Accept extra effort, polling, or indirection rather than pushing logic into C.
2. **Is it intrinsically a compositor concern?** Only these legitimately belong in the shim:
   - keyboard/pointer **seat focus** of wlr surfaces (the shell cannot focus arbitrary surfaces),
   - **input routing**, surface/**layer ordering**, output/mode handling,
   - the compositor's own **IPC feed** (e.g. the pointer broadcast the shell subscribes to).
3. **Is there truly no other way?** Only then add C — and keep it the minimal seat/surface
   primitive, leaving the *policy* (when/whether) in Kotlin.

## Why
The Kotlin side is testable, hot-reloadable, portable across JVM (`runDesktop`) and nested, and
where the project's whole architecture lives. Native compositor code is fragile, hard to debug,
and couples behavior to one backend. Logic split into C tends to rot and surprise.

## Corollary: JVM must work first
A behavior must work in plain **JVM / `runDesktop`** first — that is the source of truth. The
nested compositor path is for compositor-specific features or final integration, not where new
behavior is born. If a fix only works nested, it is not done.

## Review checklist for native diffs
When a change touches `shim.c` (or any native code), challenge it:
- Could this live in Kotlin instead? If yes, move it.
- Does the C part stay a thin primitive, with the decision logic in the shell?
- Example of a *correct* split: focus-follows-mouse sets wlr seat focus in C (unavoidable), but
  dock/control-center **autohide is driven by pointer-leave in the Kotlin shell**, not in C.
