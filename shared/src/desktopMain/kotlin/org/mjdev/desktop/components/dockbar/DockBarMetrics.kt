package org.mjdev.desktop.components.dockbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import org.mjdev.desktop.interfaces.ITheme

object DockBarMetrics {
    fun expandedHeight(
        theme: ITheme,
        iconSize: DpSize,
        iconPadding: PaddingValues,
        iconOuterPadding: PaddingValues,
    ): Dp =
        iconSize.height +
            iconPadding.calculateTopPadding() +
            iconPadding.calculateBottomPadding() +
            iconOuterPadding.calculateTopPadding() +
            iconOuterPadding.calculateBottomPadding() +
            theme.panelContentPadding * 2
}
