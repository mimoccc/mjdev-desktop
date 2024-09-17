package eu.mjdev.desktop.helpers

import androidx.compose.animation.*

object Animations {
    val ControlCenterEnterAnimation = fadeIn() + slideInHorizontally(initialOffsetX = { it })
    val ControlCenterExitAnimation = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()

    val DesktopPanelEnterAnimation = fadeIn() + slideInVertically(initialOffsetY = { it })
    val DesktopPanelExitAnimation = slideOutVertically(targetOffsetY = { it }) + fadeOut()

    val AppsMenuEnterAnimation = fadeIn() + slideInVertically(initialOffsetY = { it })
    val AppsMenuExitAnimation = slideOutVertically(targetOffsetY = { it }) + fadeOut()
}