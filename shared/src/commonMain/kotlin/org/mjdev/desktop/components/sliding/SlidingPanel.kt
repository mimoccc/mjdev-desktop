package org.mjdev.desktop.components.sliding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync
import org.mjdev.desktop.extensions.Modifier.onMouseEnter
import org.mjdev.desktop.extensions.Modifier.onMouseLeave
import org.mjdev.desktop.helpers.animation.Animations.DefaultEnterAnimation
import org.mjdev.desktop.helpers.animation.Animations.DefaultExitAnimation

// todo remove, move to chrome window
@Suppress("FunctionName")
@Composable
fun SlidingPanel(
    modifier: Modifier = Modifier,
    state: VisibilityState = rememberVisibilityState(),
    orientation: Orientation = Horizontal,
    enterAnimation: EnterTransition = DefaultEnterAnimation,
    exitAnimation: ExitTransition = DefaultExitAnimation,
    onPointerEnter: () -> Unit = {
        runAsync {
            if (!state.isVisible) {
                state.show()
            } else {
                state.focus()
            }
        }
    },
    onPointerLeave: () -> Unit = {
//        state.hide() // or unfocus or nothing
    },
    content: @Composable BoxScope.(isVisible: Boolean) -> Unit = {},
) {
    val innerContent: @Composable () -> Unit = {
        when (orientation) {
            Horizontal ->
                Box(
                    modifier =
                        modifier
                            .fillMaxHeight()
                            .onMouseEnter { onPointerEnter() }
                            .onMouseLeave { onPointerLeave() },
                ) {
                    content(state.isVisible)
                }

            Vertical ->
                Box(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .onMouseEnter { onPointerEnter() }
                            .onMouseLeave { onPointerLeave() },
                ) {
                    content(state.isVisible)
                }
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        enter = enterAnimation,
        exit = exitAnimation,
        visible = state.isVisible,
        content = {
            innerContent()
        },
    )
}

@Preview
@Composable
fun PreviewSlidingMenu() =
    preview {
        SlidingPanel()
    }
