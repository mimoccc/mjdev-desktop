/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.grid.base

import androidx.compose.runtime.Composable

@GridPlacedScopeMarker
sealed interface GridPlacedScope {
    fun item(
        row: Int,
        column: Int,
        rowSpan: Int = 1,
        columnSpan: Int = 1,
        itemContent: @Composable GridPlacedItemScope.() -> Unit
    )

    fun item(
        rowSpan: Int = 1,
        columnSpan: Int = 1,
        itemContent: @Composable GridPlacedItemScope.() -> Unit
    )
}
