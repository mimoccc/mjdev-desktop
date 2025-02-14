package org.mjdev.desktop.components.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import org.mjdev.desktop.components.sliding.base.VisibilityState

@Composable
fun ChromeWindow(
    modifier: Modifier = Modifier,
    visibilityState: VisibilityState,
    enter: EnterTransition = fadeIn() + expandIn(),
    exit: ExitTransition = shrinkOut() + fadeOut(),
    onPlaced: (LayoutCoordinates) -> Unit,
    content: @Composable () -> Unit
) = AnimatedVisibility(
    enter = enter,
    exit = exit,
    visible = visibilityState.isVisible,
    modifier = modifier.onPlaced { coordinates -> onPlaced(coordinates) },
    content = { content() }
)