package eu.mjdev.desktop.components.immersivelist

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

object ImmersiveListDefaults {
    val EnterTransition: EnterTransition = fadeIn(animationSpec = tween(300))
    val ExitTransition: ExitTransition = fadeOut(animationSpec = tween(300))
}