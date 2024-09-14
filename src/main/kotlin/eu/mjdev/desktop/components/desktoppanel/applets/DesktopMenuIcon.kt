package eu.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.desktoppanel.DesktopPanelIcon

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
        onToolTip = onTooltip,
        onClick = onClick
    )
    Divider(
        modifier = Modifier
            .padding(2.dp)
            .height(iconSize.height - 8.dp)
            .width(2.dp)
            .background(Color.White.copy(alpha = 0.4f))
    )
}