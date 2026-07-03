package org.mjdev.desktop.components.tooltip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.windows.ChromeWindow
import org.mjdev.desktop.windows.ChromeWindowState
import org.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
import java.awt.MouseInfo

/**
 * Standalone tooltip window — a small, transparent, non-focusable, always-on-top
 * [ChromeWindow] shown next to the mouse pointer whenever [TooltipState] carries a value
 * (components push into it via their `onTooltip` callbacks). It auto-hides: after
 * [hideDelay] the content fades out over [fadeDuration] and the window disappears.
 * No hover-tracking popup logic — show, wait, fade, gone.
 */
@Suppress("FunctionName")
@Composable
fun TooltipWindow(
    tooltipState: TooltipState = rememberTooltipState(),
    hideDelay: Long = 3_000L,
    fadeDuration: Int = 400,
    size: DpSize = DpSize(360.dp, 96.dp),
    cursorOffset: DpOffset = DpOffset(16.dp, 24.dp),
    windowState: ChromeWindowState = rememberChromeWindowState(),
) = withDesktopContext {
    var contentVisible by remember { mutableStateOf(false) }
    val item = tooltipState.value
    LaunchedEffect(item) {
        if (item != null) {
            windowState.position = pointerPosition(cursorOffset, size, containerSize)
            contentVisible = true
            delay(hideDelay)
            contentVisible = false
            delay(fadeDuration.toLong())
            tooltipState.clear()
        } else {
            contentVisible = false
        }
    }
    ChromeWindow(
        name = "Tooltip",
        visible = item != null,
        transparent = true,
        focusable = false,
        alwaysOnTop = true,
        size = size,
        windowState = windowState,
        onCreated = {
            windowState.size = size
        },
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(fadeDuration)),
            exit = fadeOut(tween(fadeDuration)),
        ) {
            Tooltip(tooltipState = tooltipState)
        }
    }
}

/**
 * Window position next to the current mouse pointer, clamped so the tooltip never
 * leaves the screen. AWT reports the pointer in px; shell windows are positioned 1:1
 * px-to-dp (see Window.setPositionImpl), so the raw values are used as dp directly.
 */
private fun pointerPosition(
    cursorOffset: DpOffset,
    size: DpSize,
    containerSize: DpSize,
): DpOffset {
    val pointer = runCatching { MouseInfo.getPointerInfo()?.location }.getOrNull()
    val x = (pointer?.x ?: 0).dp + cursorOffset.x
    val y = (pointer?.y ?: 0).dp + cursorOffset.y
    return DpOffset(
        x = x.coerceIn(0.dp, (containerSize.width - size.width).coerceAtLeast(0.dp)),
        y = y.coerceIn(0.dp, (containerSize.height - size.height).coerceAtLeast(0.dp)),
    )
}

@Suppress("unused")
@Preview
@Composable
fun PreviewTooltipWindow() = preview {
    TooltipWindow(
        tooltipState = rememberTooltipState().apply { show("test tooltip") },
    )
}
