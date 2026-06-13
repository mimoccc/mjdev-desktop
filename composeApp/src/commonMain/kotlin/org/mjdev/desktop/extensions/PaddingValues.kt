package org.mjdev.desktop.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

object PaddingValues {
    val PaddingValues.width: Dp
        get() = calculateLeftPadding(LayoutDirection.Ltr) + calculateRightPadding(LayoutDirection.Ltr)

    val Dp.sp: TextUnit
        get() = value.sp

    val PaddingValues.height: Dp
        get() = calculateTopPadding() + calculateBottomPadding()

    val PaddingValues.size: DpSize
        get() = DpSize(width, height)
}
