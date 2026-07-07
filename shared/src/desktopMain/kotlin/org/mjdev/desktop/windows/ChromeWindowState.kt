package org.mjdev.desktop.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.extensions.LaunchedEffect.LaunchedEffect
import org.mjdev.desktop.extensions.MutableStateExt.rememberState
import org.mjdev.desktop.log.Log
import java.awt.Window

@Suppress("RedundantSuspendModifier", "MemberVisibilityCanBePrivate")
open class ChromeWindowState(
    position: DpOffset = DpOffset.Zero,
    size: DpSize = DpSize.Zero,
    closeAction: WindowCloseAction,
    visible: Boolean = false,
    enabled: Boolean = true,
    hideDelay: Long = 0L,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : VisibilityState(
        visible = visible,
        enabled = enabled,
        hideDelay = hideDelay,
        scope = scope,
    ),
    WindowState {
    private var isCreated = false

    private var onFocusChange: MutableList<ChromeWindowState.(focus: Boolean) -> Unit> =
        mutableStateListOf()
    private var onOpened: MutableList<ChromeWindowState.() -> Unit> =
        mutableStateListOf()
    private var onClosed: MutableList<ChromeWindowState.() -> Unit> =
        mutableStateListOf()

    var window: Window? = null
        get() =
            field ?: run {
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

    val focusHelper: WindowFocusListener =
        WindowFocusListener { _, focus ->
            this@ChromeWindowState.onFocusChange.forEach { listener ->
                listener.invoke(this@ChromeWindowState, focus)
            }
        }

    val stateHelper: WindowStateListener =
        WindowStateListener({
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

    // Guard: when true the position/size setters only update the backing field and skip their
    // async apply coroutines — [applyBounds] is driving the window directly and atomically.
    private var applyingBounds = false

    override var position: DpOffset = position
        set(value) {
            Log.d("ChromeWindow position: $field -> $value")
            field = value
            if (isCreated && !applyingBounds) {
                scope.launch {
                    setPosition(value)
                }
            }
        }

    override var size: DpSize = size
        set(value) {
            Log.d("ChromeWindow size: $field -> $value")
            val oldSize = field
            field = value
            if (isCreated && !applyingBounds) {
                scope.launch {
                    moveBy(
                        value.width - oldSize.width,
                        value.height - oldSize.height,
                    )
                    setSize(value)
                }
            }
        }

    /**
     * Sets position AND size to absolute values in ONE ordered coroutine, with no relative
     * moveBy. The default reactive setters each launch their own coroutine and the size setter
     * uses a relative moveBy — for a bottom-anchored window (the dock) those race and land the
     * window at the wrong position/size (grown to full height but still at the collapsed anchor,
     * so only a thin sliver is on-screen). Driving both absolutely here is order-independent, so
     * the final geometry is always exactly `(position, size)`.
     */
    suspend fun applyBounds(
        position: DpOffset,
        size: DpSize,
    ) {
        applyingBounds = true
        this.size = size // backing field only — no async apply (guard is set)
        this.position = position
        applyingBounds = false
        // size first (AWT keeps the top-left fixed and extends downward), then the absolute
        // position pulls the top up to the anchor — the reverse would flash off-screen.
        setSize(size)
        setPosition(position)
    }

    override var isMinimized: Boolean = false

    val x: Dp
        get() = position.x

    val y: Dp
        get() = position.y

    /** name + intended position/size + actual AWT bounds, for logging the window geometry */
    private fun geom(): String {
        val w = if (isCreated) window else null
        val bounds = w?.let { " bounds=[${it.x},${it.y} ${it.width}x${it.height}]" } ?: ""
        return "name=${w?.name ?: "-"} pos=$position size=$size$bounds"
    }

    override suspend fun show() {
        Log.d("ChromeWindow show: ${geom()}")
        super.show()
    }

    override suspend fun hide(force: Boolean) {
        Log.d("ChromeWindow hide(force=$force): ${geom()}")
        super.hide(force)
    }

    fun onOpened(block: ChromeWindowState.() -> Unit) {
        onOpened.add(block)
    }

    fun onClosed(block: ChromeWindowState.() -> Unit) {
        onClosed.add(block)
    }

    fun onFocusChange(block: ChromeWindowState.(Boolean) -> Unit) {
        onFocusChange.add(block)
    }

    suspend fun moveBy(
        x: Dp,
        y: Dp,
    ) = runCatching {
        Log.d("ChromeWindow moveBy ($x, $y): ${geom()}")
        setPosition(
            DpOffset(
                position.x - x,
                position.y - y,
            ),
        )
    }.onFailure { e -> Log.e(e) }

    suspend fun setSize(size: DpSize) = runCatching {
        Log.d("ChromeWindow setSize -> $size: ${geom()}")
        window?.setSizeSafely(size, WindowPlacement.Floating)
    }.onFailure { e -> Log.e(e) }

    suspend fun setPosition(position: DpOffset) = runCatching {
        Log.d("ChromeWindow setPosition -> $position: ${geom()}")
        window?.setPosition(
            WindowPosition.Absolute(position.x, position.y),
            WindowPlacement.Floating,
        )
    }.onFailure { e -> Log.e(e) }

    override suspend fun focus() {
        runCatching {
            Log.d("ChromeWindow focus: ${geom()}")
            window?.toFront()
            if (!isFocused) {
                window?.requestFocus()
            }
        }.onFailure { e -> Log.e(e) }
    }

    /** Raises the window above others without stealing keyboard focus (unlike [focus]). */
    suspend fun raise() {
        runCatching {
            Log.d("ChromeWindow raise: ${geom()}")
            window?.toFront()
        }.onFailure { e -> Log.e(e) }
    }

    suspend fun showOrFocus() {
        if (isNotVisible) {
            show()
        } else {
            focus()
        }
    }

    companion object {
        @Composable
        fun rememberChromeWindowState(
            position: DpOffset = DpOffset.Unspecified,
            size: DpSize = DpSize.Zero,
            placement: WindowPlacement = WindowPlacement.Floating,
            closeAction: WindowCloseAction = WindowCloseAction.CLOSE,
            visible: Boolean = false,
            enabled: Boolean = true,
            hideDelay: Long = 0L,
            scope: CoroutineScope = rememberCoroutineScope(),
        ) = remember(
            position,
            size,
            placement,
            closeAction,
            visible,
            enabled,
            hideDelay,
            scope,
        ) {
            ChromeWindowState(
                position,
                size,
                closeAction,
                visible,
                enabled,
                hideDelay,
                scope,
            )
        }

        @Composable
        fun rememberWnState() = rememberState(false)

        @Composable
        fun updateWindowState(
            visible: Boolean,
            wnState: MutableState<Boolean>,
            state: ChromeWindowState,
        ) = LaunchedEffect {
            if (wnState.value == wnState.value && !visible) {
                if (state.closeAction == WindowCloseAction.CLOSE) {
                    wnState.value = false
                }
            }
        }
    }
}
