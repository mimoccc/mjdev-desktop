package eu.mjdev.desktop.windows

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import eu.mjdev.desktop.extensions.Compose.setWindowBounds
import eu.mjdev.desktop.helpers.WindowFocusState.Companion.windowFocusHandler
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import java.awt.Window

@Composable
fun TopWindow(
    windowState: TopWindowState,
    resizable: Boolean = false,
    title: String = "",
    icon: Painter? = null,
    focusable: Boolean = true,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onCloseRequest: () -> Unit = {},
    onFocusChange: (hasFocus: Boolean) -> Unit = {},
    content: @Composable FrameWindowScope.() -> Unit
) = Window(
    state = windowState,
    resizable = resizable,
    undecorated = true,
    onCloseRequest = onCloseRequest,
    visible = true,
    transparent = true,
    title = title,
    icon = icon,
    focusable = focusable,
    alwaysOnTop = true,
    onPreviewKeyEvent = onPreviewKeyEvent,
    onKeyEvent = onKeyEvent,
    content = {
        windowFocusHandler { hasFocus -> onFocusChange(hasFocus) }
        windowState.window = window
        AnimatedVisibility(
            visible = windowState.isVisible,
            enter = windowState.enter,
            exit = windowState.exit,
        ) {
            content()
        }
    }
)

data class WindowBounds(
    val x: Dp = 0.dp,
    val y: Dp = 0.dp,
    val width: Dp = 0.dp,
    val height: Dp = 0.dp
)

@Suppress("MemberVisibilityCanBePrivate", "unused")
class TopWindowState(
    val containerSize: DpSize,
    override var placement: WindowPlacement,
    override var position: WindowPosition,
    override var size: DpSize = containerSize,
    override var isMinimized: Boolean = false,
    val minSize: DpSize = DpSize(0.dp, 0.dp),
    val visible: Boolean = true,
    val enter: EnterTransition = fadeIn() + expandIn(),
    val exit: ExitTransition = shrinkOut() + fadeOut(),
    val computeBounds: (visible: Boolean) -> WindowBounds = { isVisible ->
        when {
            isVisible -> WindowBounds(0.dp, 0.dp, size.width, size.height)
            else -> WindowBounds(0.dp, 0.dp, minSize.width, minSize.height)
        }
    },

    ) : WindowState {
    var isVisible: Boolean = !isMinimized && visible
        set(value) {
            field = value
            if (window != null) {
                computeBounds(value).let { bounds ->
                    window?.setWindowBounds(bounds.x, bounds.y, bounds.width, bounds.height)
                }
            }
        }

    var window: Window? = null
        set(value) {
            field = value
            if (window != null) {
                computeBounds(isVisible).let { bounds ->
                    value?.setWindowBounds(bounds.x, bounds.y, bounds.width, bounds.height)
                }
            }
        }

    fun hide() {
        isVisible = false
    }

    fun show() {
        isVisible = true
    }

    companion object {
        @Composable
        fun rememberTopWindowState(
            size: DpSize = DpSize(640.dp, 480.dp),
            minSize: DpSize = DpSize(0.dp, 0.dp),
            position: WindowPosition,
            visible: Boolean = true,
            placement: WindowPlacement = WindowPlacement.Floating,
            api: DesktopProvider = LocalDesktop.current,
            containerSize: DpSize = api.containerSize,
            enter: EnterTransition = fadeIn() + expandIn(),
            exit: ExitTransition = shrinkOut() + fadeOut(),
            computeBounds: (visible: Boolean) -> WindowBounds = { isVisible ->
                when {
                    isVisible -> WindowBounds(0.dp, 0.dp, size.width, size.height)
                    else -> WindowBounds(0.dp, 0.dp, 0.dp, 0.dp)
                }
            }
        ) = remember(visible) {
            TopWindowState(
                containerSize = containerSize,
                placement = placement,
                position = position,
                size = size,
                minSize = minSize,
                computeBounds = computeBounds,
                enter = enter,
                exit = exit,
                visible = visible
            )
        }

        @Composable
        fun rememberTopWindowState(
            size: DpSize = DpSize(640.dp, 480.dp),
            minSize: DpSize = DpSize(0.dp, 0.dp),
            position: Alignment = Alignment.BottomCenter,
            visible: Boolean = true,
            placement: WindowPlacement = WindowPlacement.Floating,
            api: DesktopProvider = LocalDesktop.current,
            containerSize: DpSize = api.containerSize,
            enter: EnterTransition = fadeIn() + expandIn(),
            exit: ExitTransition = shrinkOut() + fadeOut(),
            computeBounds: (visible: Boolean) -> WindowBounds = { isVisible ->
                when {
                    isVisible -> WindowBounds(0.dp, 0.dp, size.width, size.height)
                    else -> WindowBounds(0.dp, 0.dp, 0.dp, 0.dp)
                }
            }
        ) = rememberTopWindowState(
            size = size,
            minSize = minSize,
            position = WindowPosition.Aligned(position),
            placement = placement,
            api = api,
            containerSize = containerSize,
            computeBounds = computeBounds,
            enter = enter,
            exit = exit,
            visible = visible
        )
    }
}
