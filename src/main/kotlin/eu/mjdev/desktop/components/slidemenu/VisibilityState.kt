package eu.mjdev.desktop.components.slidemenu

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("CanBeParameter")
class VisibilityState(
    private val scope: CoroutineScope,
    private val startState: Boolean = false,
    private val enabled: Boolean = true
) {
    var isVisible: Boolean
        get() = visible.value
        set(value) {
            visible.value = value
        }

    private val visible: MutableState<Boolean> = mutableStateOf(startState)

    fun show() {
        if (enabled) scope.launch {
            visible.value = true
        }
    }

    fun hide(delay: Long = 0L) {
        if (enabled) scope.launch {
            delay(delay)
            visible.value = false
        }
    }

    fun toggle() {
        visible.value = !visible.value
    }

    companion object {
        @Composable
        fun rememberVisibilityState(
            startState: Boolean = false,
            enabled: Boolean = true
        ): VisibilityState {
            val scope = rememberCoroutineScope()
            return remember(startState, enabled) {
                VisibilityState(scope, startState, enabled)
            }
        }
    }
}