package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.fonticon.FontIcon
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Compose.onLeftClick
import eu.mjdev.desktop.extensions.Compose.onMousePress
import eu.mjdev.desktop.extensions.Compose.onRightClick
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Composable
fun AppsMenuApp(
    modifier: Modifier = Modifier,
    app: App? = null,
    icon: String? = null,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    iconTint: Color = Color.Black,
    textColor: Color = Color.White,
    backgroundColor: Color = Color.White,
    onContextMenuClick: () -> Unit = {},
    onClick: () -> Unit = {},
) = withDesktopScope {
    Row(
        modifier = modifier.onMousePress {
            onLeftClick { onClick() }
            onRightClick { onContextMenuClick() }
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val appIconName = remember(app, icon) { app?.name ?: icon }
        val materialIcon = remember(appIconName) { iconSet.iconForName(appIconName) ?: "?".toInt() }
        FontIcon(
            iconId = materialIcon,
            iconSize = iconSize,
            iconColor = iconTint,
            iconBackgroundColor = backgroundColor,
            outerPadding = PaddingValues(2.dp),
            innerPadding = PaddingValues(0.dp),
        )
        TextAny(
            modifier = Modifier.padding(start = 4.dp).fillMaxWidth(),
            text = app?.name ?: "",
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun AppsMenuAppPreview() = AppsMenuApp()
