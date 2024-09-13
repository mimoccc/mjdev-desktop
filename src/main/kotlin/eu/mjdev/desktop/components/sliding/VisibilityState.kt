package eu.mjdev.desktop.components.sliding

import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import eu.mjdev.desktop.helpers.DpBounds
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("CanBeParameter", "unused")
class VisibilityState(
    private val scope: CoroutineScope,
    private val startState: Boolean = false,
    var enabled: Boolean = true,
    val containerSize: DpSize = DpSize.Zero
) {
    val visibleState: MutableState<Boolean> = mutableStateOf(startState)
    val boundsState: MutableState<DpBounds> = mutableStateOf(DpBounds.Zero)

    var bounds: DpBounds
        get() = boundsState.value
        set(value) {
            boundsState.value = value
            println("Bounds : $value")
        }

    var isVisible: Boolean
        get() = visibleState.value
        set(value) {
            if (enabled) {
                visibleState.value = value
            } else {
                println("Visible state disabled, can not set value.")
            }
        }

    fun show() {
        if (!isVisible) {
            if (enabled) scope.launch {
                visibleState.value = true
            }
        }
    }

    fun hide() {
        if (isVisible) {
            if (enabled) scope.launch {
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
            startState: Boolean = false,
            enabled: Boolean = true,
            api: DesktopProvider = LocalDesktop.current,
        ): VisibilityState {
            val scope = rememberCoroutineScope()
            return remember(startState, enabled, api.containerSize) {
                VisibilityState(scope, startState, enabled, api.containerSize)
            }
        }
    }
}