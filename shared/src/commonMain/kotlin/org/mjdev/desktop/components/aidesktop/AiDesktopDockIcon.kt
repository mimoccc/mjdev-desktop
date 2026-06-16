package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import org.mjdev.desktop.components.fonticon.FontIcon
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

@Composable
fun AiDesktopDockIcon(
    modifier: Modifier = Modifier,
    iconName: String,
    contentDescription: String = iconName,
    iconSize: Dp = AiDesktopMetrics.DockIconSize,
    iconColor: Color? = null,
    iconBackgroundColor: Color? = null,
    onClick: () -> Unit = {},
) = withDesktopContext {
    FontIcon(
        modifier = modifier,
        iconName = iconName,
        iconSize = DpSize(iconSize, iconSize),
        iconColor = iconColor ?: textColor,
        iconBackgroundColor = iconBackgroundColor ?: backgroundColor,
        outerPadding = PaddingValues(AiDesktopMetrics.DockIconPadding),
        innerPadding = PaddingValues(AiDesktopMetrics.DockIconPadding),
        contentDescription = contentDescription,
        onClick = onClick,
    )
}
