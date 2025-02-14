package org.mjdev.desktop.components.desktoppanel.applets

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
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

// todo
@Composable
fun DesktopPanelTray(
    modifier: Modifier = Modifier,
    round: Dp = 8.dp,
    padding: PaddingValues = PaddingValues(
        horizontal = 4.dp
    ),
    content: @Composable RowScope.() -> Unit = {}
) = withDesktopContext {
    Row(
        modifier = modifier.background(backgroundColor, RoundedCornerShape(round)).padding(padding),
        content = content
    )
}

//@Preview
@Suppress("unused")
@Composable
fun DesktopPanelTrayPreview() = preview {
    DesktopPanelTray()
}
