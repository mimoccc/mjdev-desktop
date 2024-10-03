package eu.mjdev.desktop.components.immersivelist

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

@Immutable
class ImmersiveListScope internal constructor(
    private val onFocused: (Int) -> Unit
) {
    fun Modifier.immersiveListItem(index: Int): Modifier {
        return this then onFocusChanged {
            if (it.isFocused) onFocused(index)
        }
    }
}