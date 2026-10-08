package org.psippr.desktop.managers.palette

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Color steps of a palette change. Uses the same easing as the wallpaper crossfade (Compose
 * `tween` default), so colors and picture move together.
 */
object PaletteTransition {
    /** Time between two color steps, in milliseconds (about 30 steps per second). */
    const val FRAME_MS = 33L

    /** Number of steps of a transition lasting [durationMs]; at least one (the final color). */
    fun steps(durationMs: Long): Int = (durationMs / FRAME_MS).coerceAtLeast(1L).toInt()

    /** Color between [start] and [target] at [fraction] (0..1) of the transition. */
    fun colorAt(
        start: Color,
        target: Color,
        fraction: Float,
    ): Color = lerp(start, target, FastOutSlowInEasing.transform(fraction.coerceIn(0f, 1f)))
}
