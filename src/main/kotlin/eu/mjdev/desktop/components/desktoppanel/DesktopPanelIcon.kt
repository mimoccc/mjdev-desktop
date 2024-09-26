package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.fonticon.FontIcon
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Compose.color
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.extensions.Modifier.clipRect
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DesktopPanelIcon(
    api: DesktopProvider = LocalDesktop.current,
    app: App? = null,
    isRunning: Boolean = false,
    isStarting: Boolean = false,
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
    onClick: () -> Unit = {},
    onContextMenuClick: () -> Unit = {}
) {
    val materialIcon = remember(app, icon) {
        val iconName = app?.name ?: icon // todo better guess
        api.currentUser.theme.iconSet.iconForName(iconName) ?: "?".toInt()
    }
    val background = when {
        iconState.value -> iconBackgroundHover
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .size(iconSize)
            .clipRect()
            .onPointerEvent(PointerEventType.Enter) {
                iconState.value = true
                onToolTip(app ?: contentDescription)
            }
            .onPointerEvent(PointerEventType.Exit) {
                iconState.value = false
            }
            .onPointerEvent(PointerEventType.Press) {
                iconState.value = false
            }
    ) {
        Button(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            onClick = onClick,
            colors = ButtonDefaults.color(background),
            elevation = ButtonDefaults.noElevation()
        ) {
            FontIcon(
                iconId = materialIcon,
                iconColor = iconColor,
                iconBackgroundColor = if (isRunning) iconColorRunning else iconBackgroundColor,
                iconSize = iconSize,
                innerPadding = iconPadding,
                outerPadding = iconOuterPadding,
                onClick = onClick,
                onRightClick = onContextMenuClick
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
