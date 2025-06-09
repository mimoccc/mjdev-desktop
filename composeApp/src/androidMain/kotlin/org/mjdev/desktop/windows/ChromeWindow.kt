package org.mjdev.desktop.windows

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.context.IDesktopContext

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChromeWindow(
    modifier: Modifier = Modifier,
    content: @Composable ChromeWindowState.() -> Unit
) = withDesktopContext {
    val windowState = ChromeWindowState(context)
    Box(
        modifier = modifier
            .alpha(0f)
            .onPlaced { coords ->
                windowState.onPlaced(coords)
            },
        content = { content(windowState) }
    )
    ChromeWindowInternal(
        state = windowState
    ) {
        content(windowState)
    }
}

@Composable
fun ChromeWindowInternal(
    state: ChromeWindowState,
    content: @Composable () -> Unit
) {
    // todo
    content()
}

class ChromeWindowState(
    val context: IDesktopContext,
) {
    var size: DpSize = DpSize.Zero
    var position: DpOffset = DpOffset.Zero

    fun onPlaced(coords: LayoutCoordinates) {
        position = coords.positionOnScreen().let { pos -> DpOffset(pos.x.dp, pos.y.dp) }
        size = coords.size.let { size -> DpSize(size.width.dp, size.height.dp) }
    }
}