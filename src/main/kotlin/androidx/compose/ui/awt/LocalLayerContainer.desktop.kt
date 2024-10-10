package androidx.compose.ui.awt

import androidx.compose.runtime.compositionLocalOf
import java.awt.Container

val LocalLayerContainer = compositionLocalOf<Container> {
    error("CompositionLocal LayerContainer not provided")
}