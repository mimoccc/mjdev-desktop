package eu.mjdev.desktop.components.gallery

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import eu.mjdev.desktop.components.image.ImageColoredBackground
import eu.mjdev.desktop.extensions.Compose.onKey
import eu.mjdev.desktop.extensions.Custom.toggle

@Composable
fun BoxWithControls(
    modifier: Modifier = Modifier,
    src: Any? = null,
    contentAlignment: Alignment = Alignment.BottomCenter,
    controlsState: MutableState<Boolean> = remember { mutableStateOf(false) },
    controls: @Composable (
        src: Any?,
        bckColor: Color,
        controlsState: MutableState<Boolean>
    ) -> Unit = { _, _, _ -> },
    content: @Composable (
        src: Any?,
        bckColor: Color,
        controlsState: MutableState<Boolean>
    ) -> Unit = { _, _, _ -> }
) = ImageColoredBackground(
    modifier = modifier
//        .swipeGestures(
//            onTap = {  controlsState.toggle() }
//        )
        .onKey(Key.Enter) {
            controlsState.toggle()
        }
        .onKey(Key.DirectionDown) {
            controlsState.value = false
        }
        .onKey(Key.DirectionUp) {
            controlsState.value = true
        },
    src = src,
    contentAlignment = contentAlignment,
) { bckColor ->
    content(src, bckColor, controlsState)
    if (controlsState.value) {
        controls(src, bckColor, controlsState)
    }
}

@Preview
@Composable
fun BoxWithControlsPreview() = BoxWithControls()
