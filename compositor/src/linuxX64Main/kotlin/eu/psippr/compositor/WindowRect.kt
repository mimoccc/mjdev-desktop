/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.psippr.compositor

/** Position and size of a window in output (layout) coordinates. */
data class WindowRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)
