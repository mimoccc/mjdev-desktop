package org.mjdev.desktop.extensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize

object DpSizeExt {
    operator fun DpSize.plus(dp: Dp) = copy(width = width + dp, height = height + dp)

    operator fun DpSize.minus(dp: Dp) = copy(width = width - dp, height = height - dp)
}
