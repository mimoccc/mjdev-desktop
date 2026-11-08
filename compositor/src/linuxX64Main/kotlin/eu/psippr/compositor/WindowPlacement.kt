/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.psippr.compositor

/** Where an app's main window opens when no geometry of it is remembered yet. */
object WindowPlacement {
    /** Share of the output width and height a new window gets, in percent. */
    const val DEFAULT_SIZE_PERCENT = 90

    /** Whole output in percent. */
    private const val FULL_PERCENT = 100

    /** Divisor that splits the free space evenly on both sides (centering). */
    private const val CENTER_DIVISOR = 2

    /**
     * [rect] (a remembered geometry, maybe from another screen size) shrunk to the output and
     * moved fully onto it; null while the output size is unknown.
     */
    fun fit(
        rect: WindowRect,
        outputWidth: Int,
        outputHeight: Int,
    ): WindowRect? {
        if (outputWidth <= 0 || outputHeight <= 0) {
            return null
        }
        val width = rect.width.coerceIn(1, outputWidth)
        val height = rect.height.coerceIn(1, outputHeight)
        return WindowRect(
            x = rect.x.coerceIn(0, outputWidth - width),
            y = rect.y.coerceIn(0, outputHeight - height),
            width = width,
            height = height,
        )
    }

    /** Centered rect of [DEFAULT_SIZE_PERCENT] of the output, null while the output size is unknown. */
    fun initial(
        outputWidth: Int,
        outputHeight: Int,
    ): WindowRect? {
        if (outputWidth <= 0 || outputHeight <= 0) {
            return null
        }
        val width = outputWidth * DEFAULT_SIZE_PERCENT / FULL_PERCENT
        val height = outputHeight * DEFAULT_SIZE_PERCENT / FULL_PERCENT
        return WindowRect(
            x = (outputWidth - width) / CENTER_DIVISOR,
            y = (outputHeight - height) / CENTER_DIVISOR,
            width = width,
            height = height,
        )
    }
}
