package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.data.App

@Composable
fun DesktopPanelAppTooltip(
    app: App?,
    api: DesktopProvider = LocalDesktop.current
) = Box(
    modifier = Modifier
        .wrapContentSize()
        .background(api.currentUser.theme.backgroundColor, RoundedCornerShape(8.dp))
        .padding(4.dp)
) {
    if (app != null) {
        Column {
            if (app.name.isNotEmpty()) {
                TextAny(
                    text = app.name,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            if (app.comment.isNotEmpty()) {
                TextAny(
                    text = app.comment,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}