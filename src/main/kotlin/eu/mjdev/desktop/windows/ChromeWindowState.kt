package eu.mjdev.desktop.windows

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Window

@Suppress("RedundantSuspendModifier", "MemberVisibilityCanBePrivate")
open class ChromeWindowState(
    position: WindowPosition = WindowPosition.Aligned(Alignment.Center),
    size: DpSize = DpSize(Dp.Unspecified, Dp.Unspecified),
    placement: WindowPlacement = WindowPlacement.Floating,
    closeAction: WindowCloseAction,
    visible: Boolean = false,
    enabled: Boolean = true,
    hideDelay: Long = 0L,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    var tooltipHeight: Dp = 0.dp,
) : VisibilityState(
    startState = visible,
    enabled = enabled,
    hideDelay = hideDelay,
    scope = scope
), WindowState {

    private var isCreated = false

    private var onFocusChange: MutableList<ChromeWindowState.(focus: Boolean) -> Unit> = mutableStateListOf()
    private var onOpened: MutableList<ChromeWindowState.() -> Unit> = mutableStateListOf()
    private var onClosed: MutableList<ChromeWindowState.() -> Unit> = mutableStateListOf()

    var window: Window? = null
        get() = field ?: run {
            Log.e("Window is empty, implementation error.")
            null
        }
        set(value) {
            if (value != null) {
                field = value
                isCreated = true
            } else {
                Log.e("Attempt to set window value to null.")
            }
        }

    val focusHelper: WindowFocusListener = WindowFocusListener { _, focus ->
        this@ChromeWindowState.onFocusChange.forEach { listener ->
            listener.invoke(this@ChromeWindowState, focus)
        }
    }

    val stateHelper: WindowStateListener = WindowStateListener({
        this@ChromeWindowState.onOpened.forEach { state ->
            state.invoke(this@ChromeWindowState)
        }
    }, {
        this@ChromeWindowState.onClosed.forEach { state ->
            state.invoke(this@ChromeWindowState)
        }
    })

    @Suppress("CanBePrimaryConstructorProperty")
    val closeAction: WindowCloseAction = closeAction

    val isFocused
        get() = window?.isFocused ?: false

    override var position: WindowPosition = position
        set(value) {
            Log.i("Setting position of window : ${window?.name}")
            field = value
            if (isCreated) {
                scope.launch {
                    setPosition(value)
                }
            }
        }

    override var size: DpSize = size
        set(value) {
            Log.i("Setting size of window : ${window?.name}")
            val oldSize = field
            field = value
            if (isCreated) {
                scope.launch {
                    moveBy(
                        value.width - oldSize.width,
                        value.height - oldSize.height
                    )
                    setSize(value)
                }
            }
        }

    @Suppress("CanBePrimaryConstructorProperty")
    override var placement: WindowPlacement = placement

    override var isMinimized: Boolean = false

    val x: Dp
        get() = position.x

    val y: Dp
        get() = position.y

    val height: Dp
        get() = size.height

    val width: Dp
        get() = size.width

    fun onOpened(block: ChromeWindowState.() -> Unit) {
        onOpened.add(block)
    }

    fun onClosed(block: ChromeWindowState.() -> Unit) {
        onClosed.add(block)
    }

    fun onFocusChange(block: ChromeWindowState.(Boolean) -> Unit) {
        onFocusChange.add(block)
    }

    suspend fun moveBy(x: Dp, y: Dp) = runCatching {
        Log.i("moveBy called ($x, $y)")
        if (position is WindowPosition.Absolute) {
            setPosition(
                WindowPosition.Absolute(
                    position.x - x,
                    position.y - y
                )
            )
        } else {
            setPosition(position)
        }
    }.onFailure { e -> Log.e(e) }

    suspend fun setSize(size: DpSize) = runCatching {
        Log.i("Setting window size : ${window?.name} to $size")
        window?.setSizeSafely(size, placement)
    }.onFailure { e -> Log.e(e) }

    suspend fun setPosition(position: WindowPosition) = runCatching {
        Log.i("Setting window position : ${window?.name} to $position")
        window?.setPosition(position, placement)
    }.onFailure { e -> Log.e(e) }

    suspend fun requestFocus() = runCatching {
        Log.i("Requesting window focus : ${window?.name}")
        window?.toFront()
        if (!isFocused) {
            window?.requestFocus()
        }
    }.onFailure { e -> Log.e(e) }

    suspend fun showOrFocus() {
        if (isNotVisible) {
            show()
        }
        requestFocus()
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
            hideDelay: Long = 0L,
            tooltipHeight: Dp = 0.dp,
            scope: CoroutineScope = rememberCoroutineScope()
        ) = remember(
            position,
            size,
            placement,
            closeAction,
            visible,
            enabled,
            hideDelay,
            scope,
            tooltipHeight
        ) {
            ChromeWindowState(position, size, placement, closeAction, visible, enabled, hideDelay, scope, tooltipHeight)
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
}
