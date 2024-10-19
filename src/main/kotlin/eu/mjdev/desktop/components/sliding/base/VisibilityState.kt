package eu.mjdev.desktop.components.sliding.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import eu.mjdev.desktop.log.Log
import kotlinx.coroutines.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class VisibilityState(
    val startState: Boolean = false,
    var enabled: Boolean = true,
    var hideDelay: Long = 0L,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    val visibleState: MutableState<Boolean> = mutableStateOf(startState)

    private var hideJob: Job? = null

    var isVisible: Boolean
        get() = visibleState.value
        set(value) {
            if (enabled) {
                visibleState.value = value
            } else {
                visibleState.value = startState
                Log.i("Visible state disabled, can not set value.")
            }
        }

    val isNotVisible
        get() = !isVisible

    fun show() {
        hideJob?.cancel()
        visibleState.value = true
    }

    fun hide() {
        if (!enabled) return
        hideJob?.cancel()
        if (hideDelay == 0L) {
            visibleState.value = false
        } else {
            hideJob = scope.launch {
                delay(hideDelay)
                visibleState.value = false
            }
        }
    }

    fun toggle() {
        visibleState.value = !visibleState.value
    }

    companion object {
        @Composable
        fun rememberVisibilityState(
            visible: Boolean = false,
            enabled: Boolean = true,
        ): VisibilityState {
            return remember(visible, enabled) {
                VisibilityState(visible, enabled)
            }
        }
    }
}