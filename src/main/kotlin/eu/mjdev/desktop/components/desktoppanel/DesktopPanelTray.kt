package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

// todo
@Composable
fun DesktopPanelTray(
    modifier: Modifier = Modifier,
    round: Dp = 8.dp,
    padding: PaddingValues = PaddingValues(
        horizontal = 4.dp
    ),
    content: @Composable RowScope.() -> Unit = {}
) = withDesktopScope {
    Row(
        modifier = modifier.background(backgroundColor, RoundedCornerShape(round)).padding(padding),
        content = content
    )
}