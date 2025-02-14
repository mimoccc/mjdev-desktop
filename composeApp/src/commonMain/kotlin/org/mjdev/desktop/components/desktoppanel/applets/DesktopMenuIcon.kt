package org.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.extensions.Compose.preview

@Suppress("FunctionName")
@Composable
fun DesktopMenuIcon(
    modifier: Modifier = Modifier,
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    onTooltip: (item: Any?) -> Unit = {},
    onClick: () -> Unit = {},
    onContextMenuClick: () -> Unit = {}
) = Row(
    modifier = modifier
) {
    DesktopPanelIcon(
        contentDescription = "System menu",
        icon = "menu",
        iconColor = iconColor,
        iconBackgroundColor = iconBackgroundColor,
        iconBackgroundHover = Color.White.copy(alpha = 0.4f),
        iconSize = iconSize,
        iconPadding = iconPadding,
        iconOuterPadding = iconOuterPadding,
        onTooltip = onTooltip,
        onClick = onClick,
        onContextMenuClick = onContextMenuClick
    )
}

//@Preview
@Suppress("unused")
@Composable
fun DesktopMenuIconPreview() = preview {
    DesktopMenuIcon()
}
