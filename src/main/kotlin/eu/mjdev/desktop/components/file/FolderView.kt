package eu.mjdev.desktop.components.file

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.custom.MeasureUnconstrainedView
import eu.mjdev.desktop.components.grid.GridPlaced
import eu.mjdev.desktop.components.grid.base.GridPlacedCells
import eu.mjdev.desktop.components.grid.base.GridPlacedPlacementPolicy
import eu.mjdev.desktop.components.guide.GuideLines
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.data.DesktopFolderItem
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import java.io.File
import kotlin.math.max

// DefaultShellFolder
// ShellFolder
// Clipboard
// XBaseWindow
// XWM
// XWindow
// XWrapperBase
// ShellFolderManager
// X11GraphicsEnvironment

@Suppress("FunctionName")
@Composable
fun FolderView(
    modifier: Modifier = Modifier,
    showHidden: Boolean = false,
    directoryFirst: Boolean = true,
    showHomeFolder: Boolean = false,
    iconSpacing: Dp = 2.dp,
    iconSize: DpSize = DpSize(128.dp, 128.dp),
    path: File? = File("/"),
    orientation: Orientation = Orientation.Vertical, // todo
    guideVisibleState: VisibilityState = rememberVisibilityState(false),
) = withDesktopScope {
    val files by rememberFiles(path, api.homeDir, showHidden, directoryFirst, showHomeFolder)
    MeasureUnconstrainedView(
        viewToMeasure = {
            FolderIcon(
                modifier = Modifier.padding(iconSpacing),
                iconSize = iconSize,
            )
        }
    ) { mw, mh ->
        val rows = max(1, ((api.containerSize.height / mh).toInt()))
        val columns = max(1, (files.size.div(rows)))
        Box(
            modifier = modifier,
            contentAlignment = Alignment.TopStart
        ) {
            GridPlaced(
                cells = GridPlacedCells(
                    rowCount = rows,
                    columnCount = columns
                ),
                placementPolicy = GridPlacedPlacementPolicy(
                    mainAxis = GridPlacedPlacementPolicy.MainAxis.VERTICAL,
                    horizontalDirection = GridPlacedPlacementPolicy.HorizontalDirection.START_END,
                    verticalDirection = GridPlacedPlacementPolicy.VerticalDirection.TOP_BOTTOM
                ),
            ) {
                files.forEach { file ->
                    item {
                        FolderIcon(
                            modifier = Modifier.padding(iconSpacing),
                            path = file.path,
                            customName = file.customName,
                            iconSize = iconSize,
                            onDragStart = {
                                guideVisibleState.show()
                            },
                            onDragEnd = {
                                guideVisibleState.hide()
                            }
                        )
                    }
                }
            }
            GuideLines(
                modifier = modifier,
                cellSize = DpSize(mw, mh),
                color = iconsTintColor.alpha(0.5f),
                lineSize = 1.dp,
                visible = guideVisibleState.isVisible
            )
        }
    }
}

@Suppress("SimplifyBooleanWithConstants")
@Composable
private fun rememberFiles(
    path: File? = null,
    homeDir: File? = null,
    showHidden: Boolean = true,
    directoryFirst: Boolean = true,
    showHomeFolder: Boolean = true
) = remember(path, homeDir, showHidden, directoryFirst, showHomeFolder) {
    derivedStateOf {
        path?.listFiles()
            ?.sorted()
            ?.filter { f ->
                showHidden == true || (!f.name.startsWith("."))
            }
            ?.sortedByDescending {
                directoryFirst == false || it.extension.isEmpty() // todo isDirectory
            }
            ?.map { path ->
                DesktopFolderItem(path)
            }
            ?.toMutableList()
            ?.apply {
                if (showHomeFolder) add(
                    DesktopFolderItem(
                        path = homeDir ?: File("/"),
                        customName = "Home",
                        priority = 1
                    )
                )
            }
            ?.sortedByDescending {
                it.priority
            } ?: emptyList()
    }
}

@Preview
@Composable
fun FolderViewPreview() = FolderView()
