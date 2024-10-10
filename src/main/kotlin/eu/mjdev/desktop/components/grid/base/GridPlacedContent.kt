/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.grid.base

import androidx.compose.runtime.Composable

class GridPlacedContent(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val item: @Composable GridPlacedItemScope.() -> Unit
) {
    val rowSpan: Int = bottom - top + 1
    val columnSpan: Int = right - left + 1
}
