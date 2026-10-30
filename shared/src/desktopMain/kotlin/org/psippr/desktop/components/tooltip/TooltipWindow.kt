package org.psippr.desktop.components.tooltip

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.windows.ChromeWindow
import org.psippr.desktop.windows.ChromeWindowState
import org.psippr.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState
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
    var alignment by remember { mutableStateOf(Alignment.TopStart) }
    val item = tooltipState.value
    LaunchedEffect(item) {
        if (item != null) {
            // old: only the window flipped, the bubble stayed at its left edge, 360 dp away
//            windowState.position = pointerPosition(cursorOffset, size, containerSize)
            val spot = pointerSpot(cursorOffset, size, containerSize)
            alignment = spot.alignment
            windowState.position = spot.position
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
        // the bubble hugs the window side facing the pointer (see TooltipSpot)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = alignment) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(fadeDuration)),
                exit = fadeOut(tween(fadeDuration)),
            ) {
                Tooltip(tooltipState = tooltipState)
            }
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
    // old: clamping at the screen edge pushed the window under the pointer, over the tab icons
//    val x = (pointer?.x ?: 0).dp + cursorOffset.x
//    val y = (pointer?.y ?: 0).dp + cursorOffset.y
//    return DpOffset(
//        x = x.coerceIn(0.dp, (containerSize.width - size.width).coerceAtLeast(0.dp)),
//        y = y.coerceIn(0.dp, (containerSize.height - size.height).coerceAtLeast(0.dp)),
//    )
    return TooltipPlacement.position(
        pointerX = (pointer?.x ?: 0).dp,
        pointerY = (pointer?.y ?: 0).dp,
        cursorOffset = cursorOffset,
        size = size,
        containerSize = containerSize,
    )
}

/** Window position and bubble alignment next to the current mouse pointer, see [TooltipPlacement.spot]. */
private fun pointerSpot(
    cursorOffset: DpOffset,
    size: DpSize,
    containerSize: DpSize,
): TooltipSpot {
    val pointer = runCatching { MouseInfo.getPointerInfo()?.location }.getOrNull()
    return TooltipPlacement.spot(
        pointerX = (pointer?.x ?: 0).dp,
        pointerY = (pointer?.y ?: 0).dp,
        cursorOffset = cursorOffset,
        size = size,
        containerSize = containerSize,
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
