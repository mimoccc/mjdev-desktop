package org.mjdev.desktop.helpers.animation

import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

object Animations {
    val DefaultEnterAnimation = fadeIn() + expandIn()
    val DefaultExitAnimation = shrinkOut() + fadeOut()

    val ControlCenterEnterAnimation = fadeIn() + slideInHorizontally(initialOffsetX = { it })
    val ControlCenterExitAnimation = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()

    val DesktopPanelEnterAnimation = fadeIn() + slideInVertically(initialOffsetY = { it })
    val DesktopPanelExitAnimation = slideOutVertically(targetOffsetY = { it }) + fadeOut()

    val AppsMenuEnterAnimation = fadeIn() + slideInVertically(initialOffsetY = { it })
    val AppsMenuExitAnimation = slideOutVertically(targetOffsetY = { it }) + fadeOut()
}