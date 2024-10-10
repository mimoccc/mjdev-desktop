package eu.mjdev.desktop.windows

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.background.BackgroundImage
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.extensions.Compose.onLeftClick
import eu.mjdev.desktop.extensions.Compose.onMousePress
import eu.mjdev.desktop.extensions.Compose.onRightClick
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Preview
@Composable
fun DesktopWindow(
    controlCenterState: VisibilityState = rememberVisibilityState(),
    panelState: VisibilityState = rememberVisibilityState(),
    menuState: VisibilityState = rememberVisibilityState(),
//    onDesktopMenuShow: () -> Unit = {},
//    contextMenuState: ContextMenuState = rememberContextMenuState("item1", "item2"),
    content: @Composable BoxScope.() -> Unit = {}
) = withDesktopScope {
    FullScreenWindow(
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
        Box(
            modifier = Modifier.fillMaxSize()
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
                    .onMousePress {
                        onLeftClick {
                            panelState.hide()
                            menuState.hide()
                            controlCenterState.hide()
                        }
                        onRightClick {
//                        contextMenuState.show()
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
}

@Preview
@Composable
fun DesktopWindowPreview() = DesktopWindow()
