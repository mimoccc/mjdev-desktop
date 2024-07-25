package eu.mjdev.desktop.helpers

import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.FrameWindowScope
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

@Suppress("MemberVisibilityCanBePrivate")
class WindowFocusState(
    private val window: ComposeWindow? = null,
    private val onFocusChange: (focused: Boolean) -> Unit = {}
) : WindowFocusListener {
    private val focusState: MutableState<Boolean> = mutableStateOf(false)

    val isFocused: Boolean
        get() = focusState.value

    override fun windowGainedFocus(p0: WindowEvent?) {
        focusState.value = true
        onFocusChange(isFocused)
    }

    override fun windowLostFocus(p0: WindowEvent?) {
        focusState.value = false
        onFocusChange(isFocused)
    }

    fun init() {
        window?.addWindowFocusListener(this)
    }

    fun destroy() {
        window?.removeWindowFocusListener(this)
    }

    companion object {
        @Composable
        fun FrameWindowScope.windowFocusHandler(
            onFocusChange: (focused: Boolean) -> Unit = {}
        ) {
            val state = remember(window) { WindowFocusState(window, onFocusChange) }
            DisposableEffect(Unit) {
                state.init()
                onDispose {
                    state.destroy()
                }
            }
        }
    }
}
