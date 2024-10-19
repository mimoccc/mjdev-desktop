/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.compose

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DpBounds(
    var x: Dp,
    var y: Dp,
    var width: Dp,
    var height: Dp
) {
    companion object {
        val Zero = DpBounds(0.dp, 0.dp, 0.dp, 0.dp)

        fun LayoutCoordinates.toDpBounds(): DpBounds = boundsInParent().let {
            DpBounds(it.left.dp, it.top.dp, it.width.dp, it.height.dp)
        }
    }
}