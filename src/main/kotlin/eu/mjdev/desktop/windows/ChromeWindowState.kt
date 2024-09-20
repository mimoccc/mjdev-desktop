package eu.mjdev.desktop.windows

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Compose.rememberState
import java.awt.Point

class ChromeWindowState(
    position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
    size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
    placement: WindowPlacement = WindowPlacement.Floating,
    closeAction: WindowCloseAction,
) : WindowState {

    var window: ComposeWindow? = null
        get() = field
        set(value) {
            field = value
            updatePositionAndSize()
        }

    var onFocusChange: MutableList<ChromeWindowState.(focus: Boolean) -> Unit> = mutableStateListOf()
    var onOpened: MutableList<ChromeWindowState.() -> Unit> = mutableStateListOf()
    var onClosed: MutableList<ChromeWindowState.() -> Unit> = mutableStateListOf()

    val x
        get() = window?.x?.dp ?: 0.dp

    val y
        get() = window?.y?.dp ?: 0.dp

    val width
        get() = window?.width?.dp ?: 0.dp

    val height
        get() = window?.height?.dp ?: 0.dp

    @Suppress("CanBePrimaryConstructorProperty")
    val closeAction: WindowCloseAction = closeAction

    override var position: WindowPosition
        set(value) {
            window?.setPositionSafely(
                value,
                placement,
                platformDefaultPosition = {
                    Point(x.value.toInt(), y.value.toInt())
//                   WindowLocationTracker.getCascadeLocationFor(window)
                }
            )
        }
        get() = window?.let { WindowPosition.Absolute(it.x.dp, it.y.dp) } ?: WindowPosition(x,y)

    override var size: DpSize
        get() = window?.let { DpSize(it.size.width.dp, it.size.height.dp) } ?: DpSize(width, height)
        set(value) {
            window?.setSizeSafely(value, placement)
        }

    override var placement: WindowPlacement
        get() = window?.placement ?: WindowPlacement.Floating
        set(value) {
            if (window != null) {
                window?.placement = value
            }
        }

    override var isMinimized: Boolean
        get() = window?.isMinimized ?: false
        set(value) {
            window?.isMinimized = value
        }

//    init {
//        this.placement = placement
//        this.size = size
//        this.position = position
//    }

    fun requestFocus() {
        window?.toFront()
    }

    fun updatePositionAndSize() {
        window?.placement = placement
        window?.setSize(size.width.value.toInt(), size.height.value.toInt())
        window?.setPositionSafely(
            position,
            placement,
            platformDefaultPosition = {
                Point(x.value.toInt(), y.value.toInt())
            }
        )
    }

    companion object {

        @Composable
        fun rememberChromeWindowState(
            position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
            size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
            placement: WindowPlacement = WindowPlacement.Floating,
            closeAction: WindowCloseAction = WindowCloseAction.CLOSE,
        ) = remember(position, size, placement, closeAction) {
            ChromeWindowState(position, size, placement, closeAction)
        }

        @Composable
        fun rememberAnimState(visible: Boolean) = rememberCalculated(visible) {
            when (visible) {
                true -> MutableTransitionState(false)
                false -> MutableTransitionState(true)
            }
        }

        @Composable
        fun rememberWnState() = rememberState(false)

        @Composable
        fun updateWindowState(
            visible: Boolean,
            animState: State<MutableTransitionState<Boolean>>,
            wnState: MutableState<Boolean>,
            state: ChromeWindowState
        ) = launchedEffect(animState.value.currentState) { currentState ->
            if (animState.value.targetState == currentState && !visible) {
                if (state.closeAction == WindowCloseAction.CLOSE) {
                    wnState.value = false
                } else {
                    state.updatePositionAndSize()
                }
            }
        }

        @Composable
        fun updateAnimState(
            visible: Boolean,
            animState: State<MutableTransitionState<Boolean>>,
            wnState: MutableState<Boolean>,
        ) = launchedEffect(visible) {
            if (visible) {
                wnState.value = true
            } else {
                animState.value.targetState = false
            }
        }

    }

    @Suppress("unused")
    enum class WindowCloseAction {
        CLOSE,
        MOVE
    }

}