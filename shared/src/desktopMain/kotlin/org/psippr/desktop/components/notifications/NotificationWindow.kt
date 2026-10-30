package org.psippr.desktop.components.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.System.currentTimeMillis
import org.psippr.desktop.windows.ChromeWindow
import org.psippr.desktop.windows.ChromeWindowState
import org.psippr.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

/** Size of the popup window; the card sits at its top. */
private val WINDOW_SIZE = DpSize(360.dp, 140.dp)

/** Distance of the popup from the top and right screen edges (beside the control center strip). */
private val MARGIN = 16.dp

/** Time the popup stays, in milliseconds. */
private const val SHOW_MS = 5_000L

/** Fade in / out time, in milliseconds. */
private const val FADE_MS = 400

/**
 * Popup of the newest notification in the top right corner, left of the control center (its
 * thin strip when closed, the whole panel when open, read from [controlCenterState]). It hides by
 * itself after [SHOW_MS]; the notification stays in the history on the control center's home
 * tab. Its close button dismisses it there too. Not focusable, so it never takes the keyboard.
 */
@Suppress("FunctionName")
@Composable
fun NotificationWindow(controlCenterState: ChromeWindowState) = withDesktopContext {
    val manager = context.notificationsManager
    val notification = manager.popup
    val windowState = rememberChromeWindowState()
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(notification?.id) {
        if (notification != null) {
            // old: beside the closed control center's strip only, so it covered the open panel
//            windowState.position =
//                DpOffset(containerSize.width - WINDOW_SIZE.width - controlCenterDividerWidth - MARGIN, MARGIN)
            val controlCenterWidth = maxOf(controlCenterState.size.width, controlCenterDividerWidth)
            windowState.position =
                DpOffset(containerSize.width - WINDOW_SIZE.width - controlCenterWidth - MARGIN, MARGIN)
            shown = true
            delay(SHOW_MS)
            shown = false
            delay(FADE_MS.toLong())
            manager.hidePopup()
        } else {
            shown = false
        }
    }
    ChromeWindow(
        name = WINDOW_NAME,
        visible = notification != null,
        transparent = true,
        focusable = false,
        alwaysOnTop = true,
        size = WINDOW_SIZE,
        windowState = windowState,
        onCreated = { windowState.size = WINDOW_SIZE },
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            AnimatedVisibility(visible = shown, enter = fadeIn(tween(FADE_MS)), exit = fadeOut(tween(FADE_MS))) {
                notification?.let { item ->
                    NotificationCard(
                        notification = item,
                        time = NotificationTime.format(item.createdAt, currentTimeMillis),
                        onDismiss = { manager.dismiss(item.id) },
                    )
                }
            }
        }
    }
}

/** Window name: the compositor keeps it above apps and never gives it the keyboard. */
private const val WINDOW_NAME = "Notification"
