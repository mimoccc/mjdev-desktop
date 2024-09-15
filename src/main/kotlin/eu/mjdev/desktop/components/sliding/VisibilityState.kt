package eu.mjdev.desktop.components.sliding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.DpSize
import eu.mjdev.desktop.helpers.DpBounds
import eu.mjdev.desktop.helpers.DpBounds.Companion.toDpBounds
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@Suppress("CanBeParameter", "unused", "MemberVisibilityCanBePrivate")
class VisibilityState(
    private val startState: Boolean = false,
    var enabled: Boolean = true,
    val containerSize: DpSize = DpSize.Zero
) {
    private val visibleState: MutableState<Boolean> = mutableStateOf(startState)
    private val boundsState: MutableState<DpBounds> = mutableStateOf(DpBounds.Zero)
    private val focusState: MutableState<Boolean> = mutableStateOf(false)

    var bounds: DpBounds
        get() = boundsState.value
        set(value) {
            boundsState.value = value
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

    val isWindowFocus
        get() = focusState.value

    fun show() {
        if (!isVisible) {
            if (enabled) {
                visibleState.value = true
            }
        }
    }

    fun hide() {
        if (isVisible) {
            if (enabled) {
                visibleState.value = false
            }
        }
    }

    fun toggle() {
        visibleState.value = !visibleState.value
    }

    fun onPlaced(lc: LayoutCoordinates) {
        bounds = lc.toDpBounds()
    }

    fun onFocusChange(focus: Boolean) {
        focusState.value = focus
    }

    companion object {
        @Composable
        fun rememberVisibilityState(
            startState: Boolean = false,
            enabled: Boolean = true,
            api: DesktopProvider = LocalDesktop.current,
        ): VisibilityState {
            return remember(startState, enabled, api.containerSize) {
                VisibilityState(startState, enabled, api.containerSize)
            }
        }
    }
}