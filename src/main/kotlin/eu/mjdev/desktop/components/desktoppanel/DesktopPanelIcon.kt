package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.fonticon.MaterialIcon
import eu.mjdev.desktop.extensions.Compose.clipCircle
import eu.mjdev.desktop.extensions.Compose.color
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.LocalDesktop
import eu.mjdev.desktop.provider.data.App

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Preview
@Composable
fun DesktopPanelIcon(
    api: DesktopProvider = LocalDesktop.current,
    app: App? = null,
    icon: String? = null,
    iconColor: Color = Color.Black,
    iconBackgroundColor: Color = Color.White,
    iconBackgroundHover: Color = Color.Red,
    iconSize: DpSize = DpSize(56.dp, 56.dp),
    iconPadding: PaddingValues = PaddingValues(4.dp),
    iconState: MutableState<Boolean> = remember { mutableStateOf(false) },
    onClick: () -> Unit = { app?.start() },
) {
    val materialIcon = api.appsProvider.iconForApp(app?.name ?: icon) ?: "?".toInt()
    Box(
        modifier = Modifier
            .size(iconSize)
            .onPointerEvent(PointerEventType.Enter) {
                if (api.windowFocusState.isFocused) {
                    iconState.value = true
                }
            }
            .onPointerEvent(PointerEventType.Exit) {
                if (api.windowFocusState.isFocused) {
                    iconState.value = false
                }
            }
    ) {
        Button(
            modifier = Modifier
                .size(iconSize)
                .padding(iconPadding),
            contentPadding = PaddingValues(0.dp),
            onClick = onClick,
            colors = ButtonDefaults.color(if (iconState.value) iconBackgroundHover else Color.Transparent),
            elevation = ButtonDefaults.noElevation()
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(iconSize)
                    .background(iconBackgroundColor, CircleShape)
                    .clipCircle()
            ) {
                MaterialIcon(
                    iconId = materialIcon,
                    modifier = Modifier
                        .size(iconSize)
                        .padding(2.dp)
                        // todo app manager
                        .onClick { onClick() },
                    tint = iconColor
                )
            }
        }
    }
}