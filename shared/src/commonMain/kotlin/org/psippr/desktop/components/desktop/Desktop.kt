package org.psippr.desktop.components.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.psippr.desktop.components.adb.AdbScreenMirror
import org.psippr.desktop.components.background.BackgroundImage
import org.psippr.desktop.components.background.BackgroundImageDefaults
import org.psippr.desktop.components.file.FolderView
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.Modifier.onLeftClick
import org.psippr.desktop.extensions.Modifier.onMouseDown
import org.psippr.desktop.extensions.Modifier.onMousePress
import org.psippr.desktop.extensions.Modifier.onRightClick

@Suppress("FunctionName", "UNUSED_PARAMETER")
@Composable
fun Desktop(
    padding: PaddingValues = PaddingValues(),
    onTooltip: (item: Any?) -> Unit = {},
    onLeftMouseClick: () -> Unit = {},
    onRightMouseClick: () -> Unit = {},
    // right click with where it happened, in pixels of the desktop
    onRightMouseClickAt: (position: Offset) -> Unit = {},
    onBackgroundChange: (Color) -> Unit = {},
    // a connected device's screen appeared (true) or went away on the desktop
    onDeviceShown: (Boolean) -> Unit = {},
    // todo
    widgets: @Composable () -> Unit = {},
) = withDesktopContext {
    // Tooltips are no longer rendered here — components push into TooltipState via
    // onTooltip and the desktop shell shows them in a standalone auto-hide window
    // (see desktopMain TooltipWindow).
    // listened, so wallpapers that finish loading after the first frame are shown too
    val wallpapers by backgroundsProperty.changes.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        BackgroundImage(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onMousePress {
                        onLeftClick {
                            onLeftMouseClick()
                        }
                        // old: read on the release, where the right button is already up
//                        onRightClick {
//                            onRightMouseClick()
//                        }
                    }.onMouseDown {
                        onRightClick {
                            onRightMouseClick()
                            currentEvent.changes.firstOrNull()?.let { change -> onRightMouseClickAt(change.position) }
                        }
                    },
            switchDelay = theme.backgroundRotationDelay,
            images = wallpapers,
            onChange = { src ->
                context.wallpaper.value = src
                context.palette
                    .apply {
                        // colors move together with the wallpaper fade-in
                        update(src, BackgroundImageDefaults.FADE_IN_DURATION_MS)
                    }.also { p ->
                        context.currentUser.theme.backgroundColor = p.backgroundColor
                        onBackgroundChange(p.backgroundColor)
                    }
            },
        )
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                FolderView(
                    modifier = Modifier.fillMaxSize(),
                    path = context.currentUser.userDirs.desktopDirectory,
                    showHomeFolder = true,
//                orientation = Orientation.Vertical,
                )
                // todo
                widgets()
//            ComposeWebView(
//                modifier = Modifier.align(Alignment.Center).size(640.dp, 480.dp)
//            )
            }
            AdbScreenMirror(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .clip(
                            RoundedCornerShape(
                                16.dp,
                                16.dp,
                                16.dp,
                                16.dp,
                            ),
                        ),
                onShownChange = onDeviceShown,
            )
        }
    }
}

// todo
@Preview
@Composable
fun PreviewDesktop() = preview {
    Desktop()
}
