package eu.mjdev.desktop.windows

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Compose.rememberState

@Suppress("MemberVisibilityCanBePrivate")
class ChromeWindowState(
    position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
    size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
    placement: WindowPlacement = WindowPlacement.Floating,
    closeAction: WindowCloseAction,
    visible: Boolean = false,
    enabled: Boolean = true,
) : VisibilityState(
    startState = visible,
    enabled = enabled
), WindowState {

    var onFocusChange: MutableList<ChromeWindowState.(focus: Boolean) -> Unit> = mutableStateListOf()
    var onOpened: MutableList<ChromeWindowState.() -> Unit> = mutableStateListOf()
    var onClosed: MutableList<ChromeWindowState.() -> Unit> = mutableStateListOf()

    private val focusState: MutableState<Boolean> = mutableStateOf(false)
    var isFocused: Boolean
        get() = focusState.value
        internal set(value) {
            focusState.value = value
        }

    val focusHelper: WindowFocusHelper = WindowFocusHelper { _, focus ->
        isFocused = focus
        this@ChromeWindowState.onFocusChange.forEach {
            it.invoke(this@ChromeWindowState, focus)
        }
    }

    val stateHelper: WindowStateHelper = WindowStateHelper({
        setSize(size)
        this@ChromeWindowState.onOpened.forEach {
            it.invoke(this@ChromeWindowState)
        }
    }, {
        this@ChromeWindowState.onClosed.forEach {
            it.invoke(this@ChromeWindowState)
        }
    })

    var window
        get() = stateHelper.window
        set(value) {
            stateHelper.window = value
        }

    @Suppress("CanBePrimaryConstructorProperty")
    val closeAction: WindowCloseAction = closeAction

    override var position: WindowPosition = position
        set(value) {
            field = value
            stateHelper.setPosition(position)
        }

    override var size: DpSize = size
        set(value) {
            field = value
            stateHelper.setSize(value)
        }

    @Suppress("CanBePrimaryConstructorProperty")
    override var placement: WindowPlacement = placement
    override var isMinimized: Boolean = false

    fun requestFocus() {
        stateHelper.requestFocus()
    }

    fun updatePositionAndSize() {
        stateHelper.setSize(size)
        stateHelper.setPosition(position)
    }

    companion object {

        @Composable
        fun rememberChromeWindowState(
            position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
            size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
            placement: WindowPlacement = WindowPlacement.Floating,
            closeAction: WindowCloseAction = WindowCloseAction.CLOSE,
            visible: Boolean = false,
            enabled: Boolean = true,
        ) = remember(position, size, placement, closeAction, visible) {
            ChromeWindowState(position, size, placement, closeAction, visible, enabled)
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