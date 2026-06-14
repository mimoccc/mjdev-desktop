package org.mjdev.desktop.components.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.adb.AdbScreenMirror
import org.mjdev.desktop.components.background.BackgroundImage
import org.mjdev.desktop.components.file.FolderView
import org.mjdev.desktop.components.tooltip.Tooltip
import org.mjdev.desktop.components.tooltip.TooltipArea
import org.mjdev.desktop.components.tooltip.TooltipPlacement
import org.mjdev.desktop.components.tooltip.TooltipState
import org.mjdev.desktop.components.tooltip.rememberTooltipState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.onLeftClick
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.extensions.Modifier.onRightClick

@Suppress("FunctionName", "UNUSED_PARAMETER")
@Composable
fun Desktop(
    tooltipState: TooltipState = rememberTooltipState(),
    padding: PaddingValues = PaddingValues(),
    onTooltip: (item: Any?) -> Unit = {},
    onLeftMouseClick: () -> Unit = {},
    onRightMouseClick: () -> Unit = {},
    onBackgroundChange: (Color) -> Unit = {},
    // todo
    widgets: @Composable () -> Unit = {},
) = withDesktopContext {
    TooltipArea(
        modifier = Modifier.fillMaxSize(),
        delayMillis = 2000,
        tooltipPlacement =
            TooltipPlacement.CursorPoint(
                alignment = Alignment.TopStart,
                offset = DpOffset(0.dp, 16.dp),
            ),
        tooltip = {
            Tooltip(
                tooltipState = tooltipState,
            )
        },
    ) {
        BackgroundImage(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onMousePress {
                        onLeftClick {
                            onLeftMouseClick()
                        }
                        onRightClick {
                            onRightMouseClick()
                        }
                    },
            switchDelay = theme.backgroundRotationDelay,
            images = backgrounds,
            onChange = { src ->
                context.palette
                    .apply {
                        update(src)
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
            )
        }
    }
}

// todo
@Preview
@Composable
fun PreviewDesktop() =
    preview {
        Desktop()
    }
