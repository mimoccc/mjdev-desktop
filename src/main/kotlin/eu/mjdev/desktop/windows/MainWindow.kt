package eu.mjdev.desktop.windows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.mouseClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isSecondaryPressed
import eu.mjdev.dadb.helpers.log
import eu.mjdev.desktop.components.background.BackgroundImage
import eu.mjdev.desktop.components.menu.ContextMenu
import eu.mjdev.desktop.components.menu.ContextMenuState
import eu.mjdev.desktop.components.menu.ContextMenuState.Companion.rememberContextMenuState
import eu.mjdev.desktop.components.sliding.VisibilityState
import eu.mjdev.desktop.helpers.Palette
import eu.mjdev.desktop.helpers.Palette.Companion.rememberPalette
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "DEPRECATION")
@Preview
@Composable
fun MainWindow(
    controlCenterState: VisibilityState,
    panelState: VisibilityState,
    menuState: VisibilityState,
    api: DesktopProvider = LocalDesktop.current,
    palette: Palette = rememberPalette(api.currentUser.theme.backgroundColor),
    onDesktopMenuShow: () -> Unit = {},
    contextMenuState: ContextMenuState = rememberContextMenuState("item1", "item2"),
    content: @Composable BoxScope.() -> Unit = {}
) = FullScreenWindow {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(api.currentUser.theme.backgroundColor)
    ) {
        BackgroundImage(
            modifier = Modifier
                .fillMaxSize()
                .mouseClickable {
                    if (buttons.isSecondaryPressed) {
                        contextMenuState.show()
                    } else {
                        panelState.hide()
                        menuState.hide()
                        controlCenterState.hide()
                    }
                },
            backgroundColor = api.currentUser.theme.backgroundColor,
            backgrounds = api.appsProvider.backgrounds + api.currentUser.config.desktopBackgrounds,
            switchDelay = api.currentUser.theme.backgroundRotationDelay,
            onChange = { src ->
                palette.apply {
                    update(src)
                }.also { p ->
                    api.currentUser.theme.backgroundColor = p.backgroundColor
                }
            }
        )
//        WidgetsPanel(
//            modifier = Modifier
//                .fillMaxSize()
//        )
        content()
    }
    ContextMenu(
        contextMenuState = contextMenuState,
        onMenuItemClick = { item -> log { "Context menu : $item" } },
        onShow = { onDesktopMenuShow() },
        onHide = {}
    )
//    launchedEffect {
//        runCatching {
//            Notification("Hello!", "I just wanted to say hello :)").apply {
//                setUrgency(Notification.UrgencyCritical)
//            }.send()
//        }
//    }
}
