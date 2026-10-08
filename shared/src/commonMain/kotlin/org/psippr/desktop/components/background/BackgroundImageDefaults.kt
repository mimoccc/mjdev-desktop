package org.psippr.desktop.components.background

/**
 * Timing of the wallpaper rotation, shared by [BackgroundImage] and the palette so the desktop
 * colors change exactly as long as the wallpaper fades.
 */
object BackgroundImageDefaults {
    /** Time one wallpaper stays before the next one, in milliseconds. */
    const val SWITCH_DELAY_MS = 60_000L

    /** Divisor of [SWITCH_DELAY_MS] giving the fade-in time. */
    private const val FADE_IN_DIVISOR = 8

    /** Divisor of [SWITCH_DELAY_MS] giving the fade-out time. */
    private const val FADE_OUT_DIVISOR = 4

    /** Fade-in of a new wallpaper, in milliseconds; the palette colors move over the same time. */
    const val FADE_IN_DURATION_MS = SWITCH_DELAY_MS / FADE_IN_DIVISOR

    /** Fade-out of the previous wallpaper, in milliseconds. */
    const val FADE_OUT_DURATION_MS = SWITCH_DELAY_MS / FADE_OUT_DIVISOR
}
