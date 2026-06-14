package org.mjdev.desktop.components.sliding.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mjdev.desktop.extensions.MutableStateExt.toggle
import org.mjdev.desktop.log.Log

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class VisibilityState(
    val visible: Boolean = false,
    var enabled: Boolean = true,
    var hideDelay: Long = 0L,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    val enabledState: MutableState<Boolean> = mutableStateOf(true)
    val visibleState: MutableState<Boolean> = mutableStateOf(true)
    val sizeState: MutableState<DpSize> = mutableStateOf(DpSize.Zero)

    private var hideJob: Job? = null

    var isVisible: Boolean
        get() = visibleState.value
        set(value) {
            if (enabledState.value) {
                visibleState.value = value
            } else {
                visibleState.value = visible
                Log.i("Visible state disabled, can not set value.")
            }
        }

    val isNotVisible
        get() = !isVisible

    val height: Dp
        get() = sizeState.value.height

    val width: Dp
        get() = sizeState.value.width

    init {
        enabledState.value = enabled
        visibleState.value = visible
    }

    open suspend fun show() {
//        println("Show called.")
        hideJob?.cancel()
        visibleState.value = true
    }

    open suspend fun hide(force: Boolean = false) {
        if (enabledState.value || force) {
//            println("Hide called.")
            hideJob?.cancel()
            if (hideDelay == 0L) {
                visibleState.value = false
            } else {
                hideJob =
                    scope.launch {
                        delay(hideDelay)
                        visibleState.value = false
                    }
            }
        }
    }

    open suspend fun toggle(
        force: Boolean = false,
        onVisibilityChange: (visible: Boolean) -> Unit = {},
    ) {
        if (enabledState.value || force) {
            println("Toggle visible state from: ${visibleState.value} to ${!visibleState.value}")
            visibleState.toggle()
            onVisibilityChange(isVisible)
        }
    }

    open suspend fun updateSize(size: DpSize) {
//        println("Size update: $size")
        this.sizeState.value = size
    }

    open suspend fun focus() {
        Log.i("Focus not implemented in this state.")
    }

    companion object {
        @Composable
        fun rememberVisibilityState(
            visible: Boolean = false,
            enabled: Boolean = true,
            autoHideDelay: Long = 0L,
        ): VisibilityState =
            remember(visible, enabled) {
                VisibilityState(visible, enabled, autoHideDelay)
            }
    }
}
