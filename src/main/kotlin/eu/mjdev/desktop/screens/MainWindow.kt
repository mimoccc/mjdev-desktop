package eu.mjdev.desktop.screens

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.appsmenu.AppsMenu
import eu.mjdev.desktop.components.background.BackgroundImage
import eu.mjdev.desktop.components.controlpanel.ControlPanel
import eu.mjdev.desktop.components.desktoppanel.DesktopPanel
import eu.mjdev.desktop.components.slidemenu.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.components.widgets.WidgetsPanel
import eu.mjdev.desktop.extensions.Compose.FullScreenWindow
import eu.mjdev.desktop.helpers.Palette.Companion.rememberPalette
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Suppress("FunctionName")
@Preview
@Composable
fun MainWindow() = FullScreenWindow {
    val api: DesktopProvider = LocalDesktop.current
    val palette = rememberPalette(api.currentUser.theme.backgroundColor)
    val panelState = rememberVisibilityState(true, api.currentUser.config.autoHidePanel)
    val controlCenterState = rememberVisibilityState()
    val menuState = rememberVisibilityState()
    Box(
        modifier = Modifier.fillMaxSize().background(api.currentUser.theme.backgroundColor)
    ) {
        BackgroundImage(
            modifier = Modifier
                .fillMaxSize()
                .onPointerEvent(PointerEventType.Enter) {
                    controlCenterState.hide(0)
                    panelState.hide(0)
                }
                .onPointerEvent(PointerEventType.Press) {
                    menuState.hide(0)
                },
            backgroundColor = api.currentUser.theme.backgroundColor,
            backgrounds = api.appsProvider.backgrounds + api.currentUser.config.desktopBackgroundUrls,
            switchDelay = api.currentUser.theme.backgroundRotationDelay,
            onChange = { src ->
                palette.update(src)
                api.currentUser.theme.backgroundColor = palette.backgroundColor
            }
        )
        WidgetsPanel(
            modifier = Modifier
                .fillMaxSize()
                .onPointerEvent(PointerEventType.Enter) {
                    controlCenterState.hide(0)
                    panelState.hide(0)
                }
                .onPointerEvent(PointerEventType.Press) {
                    menuState.hide(0)
                },
        )
        AppsMenu(
            modifier = Modifier.align(Alignment.BottomStart),
            bottomY = 64.dp,
            state = menuState,
            backgroundColor = api.currentUser.theme.backgroundColor,
        )
        DesktopPanel(
            modifier = Modifier.align(api.currentUser.theme.panelLocation.alignment),
            state = panelState,
            backgroundColor = api.currentUser.theme.backgroundColor,
            onMenuIconClicked = {
                menuState.toggle()
            },
            onVisibilityChange = { visible ->
                if (visible) {
                    controlCenterState.hide(0)
                }
            }
        )
        ControlPanel(
            modifier = Modifier.align(api.currentUser.theme.controlCenterLocation.alignment),
            state = controlCenterState,
            backgroundColor = api.currentUser.theme.backgroundColor,
            controlCenterExpandedWidth = api.currentUser.theme.controlCenterExpandedWidth,
            controlCenterDividerWidth = api.currentUser.theme.controlCenterDividerWidth,
            controlCenterIconColor = api.currentUser.theme.controlCenterIconColor,
            controlCenterIconSize = api.currentUser.theme.controlCenterIconSize,
            pages = api.controlCenterPages,
            onVisibilityChange = { visible ->
                if (visible) {
                    panelState.hide(0)
                }
            }
        )
    }
//    launchedEffect {
//        runCatching {
//            Notification("Hello!", "I just wanted to say hello :)").apply {
//                setUrgency(Notification.UrgencyCritical)
//            }.send()
//        }
//    }
}
