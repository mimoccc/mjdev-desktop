package eu.mjdev.desktop.windows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.mouseClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isSecondaryPressed
import eu.mjdev.desktop.components.background.BackgroundImage
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "DEPRECATION")
@Preview
@Composable
fun DesktopWindow(
    controlCenterState: VisibilityState,
    panelState: VisibilityState,
    menuState: VisibilityState,
    api: DesktopProvider = LocalDesktop.current,
//    onDesktopMenuShow: () -> Unit = {},
//    contextMenuState: ContextMenuState = rememberContextMenuState("item1", "item2"),
    content: @Composable BoxScope.() -> Unit = {}
) = FullScreenWindow(
//    onOpened = {
//        println("Window opened $window")
//        window?.toBack()
//    },
    onFocusChange = { focus ->
//        RuntimeException("Focus change: $it").printStackTrace()
        // todo better solution
//        val focusOwner = window?.mostRecentFocusOwner
        if (focus) {
            window?.toBack()
        }
//        focusOwner?.requestFocus()
    }
) {
//    val backgroundColor by rememberBackgroundColor(api)
    Box(
        modifier = Modifier
            .fillMaxSize()
//            .background(backgroundColor)
//            .pointerInput(Unit) {
//                awaitPointerEventScope {
//                    while (true) {
//                        awaitPointerEvent(pass = PointerEventPass.Initial)
//                            .changes
//                            .forEach(PointerInputChange::consume)
//                    }
//                }
//            }
    ) {
        BackgroundImage(
            modifier = Modifier
                .fillMaxSize()
                .mouseClickable {
                    if (buttons.isSecondaryPressed) {
//                        contextMenuState.show()
                    } else {
                        panelState.hide()
                        menuState.hide()
                        controlCenterState.hide()
                    }
                },
            onChange = { src ->
                api.palette.apply {
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
//    ContextMenu(
//        contextMenuState = contextMenuState,
//        onMenuItemClick = { item -> log { "Context menu : $item" } },
//        onShow = { onDesktopMenuShow() },
//        onHide = {}
//    )
//    launchedEffect {
//        runCatching {
//            Notification("Hello!", "I just wanted to say hello :)").apply {
//                setUrgency(Notification.UrgencyCritical)
//            }.send()
//        }
//    }
}
