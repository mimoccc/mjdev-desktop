package eu.mjdev.desktop.helpers

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DpBounds(
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp
) {
    companion object {
        val Zero = DpBounds(0.dp, 0.dp, 0.dp, 0.dp)

        fun LayoutCoordinates.toDpBounds(): DpBounds = boundsInParent().let {
            DpBounds(it.left.dp, it.top.dp, it.width.dp, it.height.dp)
        }
    }
}