package org.mjdev.desktop.state

/**
 * The single, pure decision function for the three desktop surfaces (bottom bar, apps menu,
 * control center). No Compose, no windows, no side effects — just "given the world, what should
 * be visible". This is the readable matrix the whole desktop behaviour is derived from; the
 * stateful wiring lives in [DesktopState].
 *
 * The rules, verbatim from the UX spec:
 *  1. If nothing overlaps the bottom bar, the bar is visible. It hides only when a real window
 *     overlaps it, or when the control center is open.
 *  2. When the apps menu is open, the bar is always visible.
 *  3. When the control center is open, both the bar and the menu are hidden.
 *  4. When the control center closes and the bar is not overlapped, the bar becomes visible.
 *  5. Clicking outside a surface hides what should hide (menu + control center); the bar hides
 *     only if it is overlapped (handled by re-evaluating after the intent flags clear).
 */
object DesktopPolicy {
    fun decide(input: DesktopInputs): DesktopDecision {
        val controlCenter = input.controlCenterOpen
        // rule 3: the control center takes over — the menu steps aside while it is open
        val menu = input.menuOpen && !controlCenter
        val bar =
            when {
                controlCenter -> false // rule 3: control center hides the bar
                menu -> true // rule 2: an open menu keeps the bar up
                !input.barOverlapped -> true // rules 1 & 4: nothing covers it -> visible
                else -> input.pointerInBarRevealZone // rule 1: covered -> reveal only on hover
            }
        return DesktopDecision(
            bar = bar,
            menu = menu,
            controlCenter = controlCenter,
        )
    }
}

/**
 * Everything the policy needs to make a decision. [menuOpen] / [controlCenterOpen] are user
 * *intent* (the user asked for them), the rest are facts about the world.
 */
data class DesktopInputs(
    val menuOpen: Boolean = false,
    val controlCenterOpen: Boolean = false,
    // a real application window overlaps the bar's strip — only knowable under the compositor;
    // false everywhere else (plain runDesktop), which is exactly why the bar shows by default there
    val barOverlapped: Boolean = false,
    // pointer is over the bar or its bottom reveal hotspot (used only when the bar is overlapped)
    val pointerInBarRevealZone: Boolean = false,
)

/** What each surface should actually be after applying [DesktopPolicy]. */
data class DesktopDecision(
    val bar: Boolean,
    val menu: Boolean,
    val controlCenter: Boolean,
)
