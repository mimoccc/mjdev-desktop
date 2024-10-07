package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.fonticon.FontIcon
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.App.Companion.rememberRunningIndicator
import eu.mjdev.desktop.extensions.Compose.color
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Compose.onLeftClick
import eu.mjdev.desktop.extensions.Compose.onMouseEnter
import eu.mjdev.desktop.extensions.Compose.onMouseLeave
import eu.mjdev.desktop.extensions.Compose.onMousePress
import eu.mjdev.desktop.extensions.Compose.onRightClick
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Modifier.clipRect
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Composable
fun DesktopPanelIcon(
    app: App? = null,
    icon: String? = null,
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    iconBackgroundHover: Color = Color.White,
    iconColorRunning: Color = Color.White,
    iconSize: DpSize = DpSize(48.dp, 48.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconOuterPadding: PaddingValues = PaddingValues(2.dp),
    iconState: MutableState<Boolean> = rememberState(false),
    contentDescription: String? = null,
    onToolTip: (item: Any?) -> Unit = {},
    onContextMenuClick: () -> Unit = {},
    onClick: () -> Unit = {},
) = withDesktopScope {
    val materialIcon = remember(app, icon) {
        val iconName = app?.name ?: icon // todo better guess
        iconSet.iconForName(iconName) ?: "?".toInt()
    }
    val background = when {
        iconState.value -> iconBackgroundHover
        else -> Color.Transparent
    }
    val isStarting by rememberCalculated(app) { app?.isStarting ?: false }
    val isRunning by rememberRunningIndicator(app)
    Box(
        modifier = Modifier
            .size(iconSize)
            .clipRect()
            .onMouseEnter {
                iconState.value = true
                onToolTip(app ?: contentDescription)
            }
            .onMouseLeave {
                iconState.value = false
            }
            .onMousePress {
                iconState.value = false
                onLeftClick { onClick() }
                onRightClick { onContextMenuClick() }
            }
    ) {
        Button(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.color(background),
            elevation = ButtonDefaults.noElevation(),
            onClick = {}
        ) {
            FontIcon(
                iconId = materialIcon,
                iconColor = iconColor,
                iconBackgroundColor = if (isRunning) iconColorRunning else iconBackgroundColor,
                iconSize = iconSize,
                innerPadding = iconPadding,
                outerPadding = iconOuterPadding,
            )
        }
        if (isStarting || isRunning) {
            CircularProgressIndicator(
                modifier = Modifier.padding(4.dp).size(iconSize),
                color = iconColor,
                strokeWidth = 4.dp,
                backgroundColor = Color.Transparent
            )
        }
    }
}

@Preview
@Composable
fun DesktopPanelIconPreview() = DesktopPanelIcon()
