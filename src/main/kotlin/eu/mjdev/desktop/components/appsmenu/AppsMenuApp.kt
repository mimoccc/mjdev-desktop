package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.fonticon.MaterialIcon
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.LocalDesktop
import eu.mjdev.desktop.provider.data.App

@Preview
@Composable
fun AppsMenuApp(
    modifier: Modifier = Modifier,
    app: App? = null,
    icon: String? = null,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    textColor: Color = Color.White,
    iconTint: Color = Color.White,
    api: DesktopProvider = LocalDesktop.current,
    onClick: (app: App?) -> Unit = { app?.start() }
) = Row(
    modifier = modifier.clickable { onClick(app) },
    verticalAlignment = Alignment.CenterVertically,
) {
    val materialIcon = api.appsProvider.iconForApp(app?.name ?: icon) ?: "?".toInt()
    MaterialIcon(
        modifier = modifier.size(iconSize),
        iconId = materialIcon,
        tint = iconTint
    )
    TextAny(
        modifier = Modifier.padding(start = 4.dp).fillMaxWidth(),
        text = app?.name ?: "",
        color = textColor,
        fontWeight = FontWeight.Bold
    )
}