package org.mjdev.desktop.components.file

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import okio.Path
import org.mjdev.desktop.components.custom.MeasureUnconstrainedView
import org.mjdev.desktop.components.grid.GridPlaced
import org.mjdev.desktop.components.grid.base.GridPlacedCells
import org.mjdev.desktop.components.grid.base.GridPlacedPlacementPolicy
import org.mjdev.desktop.components.guide.GuideLines
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.data.DesktopFolderItem
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.PathExt.cwd
import org.mjdev.desktop.extensions.PathExt.isDirectory
import org.mjdev.desktop.extensions.PathExt.listFiles
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import kotlin.math.max
import org.jetbrains.compose.ui.tooling.preview.Preview

// todo orientation
@Suppress("FunctionName")
@Composable
fun FolderView(
    modifier: Modifier = Modifier,
    showHidden: Boolean = false,
    directoryFirst: Boolean = true,
    showHomeFolder: Boolean = false,
    iconSpacing: Dp = 2.dp,
    iconSize: DpSize = DpSize(128.dp, 128.dp),
    innerIconPadding: PaddingValues = PaddingValues(8.dp),
    outerIconPadding: PaddingValues = PaddingValues(4.dp),
    path: Path = cwd,
//    orientation: Orientation = Orientation.Vertical, // todo
    guideVisibleState: VisibilityState = rememberVisibilityState(false),
) = withDesktopContext {
    val files by rememberFiles(
        path,
        context.currentUser.homeDir,
        showHidden,
        directoryFirst,
        showHomeFolder
    )
    MeasureUnconstrainedView(
        viewToMeasure = {
            FolderIcon(
                modifier = Modifier.padding(iconSpacing),
                iconSize = iconSize,
                outerPadding = outerIconPadding,
                innerPadding = innerIconPadding,
            )
        }
    ) { size ->
        val rows = max(1, ((context.containerSize.height / size.height).toInt()))
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
                            modifier = Modifier
                                .padding(iconSpacing),
                            path = file.path,
                            customName = file.customName,
                            iconSize = iconSize,
                            onDragStart = {
                                runAsync {
                                    guideVisibleState.show()
                                }
                            },
                            onDragEnd = {
                                runAsync {
                                    guideVisibleState.hide()
                                }
                            }
                        )
                    }
                }
            }
            GuideLines(
                modifier = modifier,
                cellSize = size,
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
    path: Path = cwd,
    homeDir: Path = cwd,
    showHidden: Boolean = true,
    directoryFirst: Boolean = true,
    showHomeFolder: Boolean = true
) = remember(path, homeDir, showHidden, directoryFirst, showHomeFolder) {
    derivedStateOf {
        path.listFiles().sorted().filter { f ->
            showHidden == true || (!f.name.startsWith("."))
        }.sortedByDescending {
            directoryFirst == false || it.isDirectory
        }.map { path ->
            DesktopFolderItem(path)
        }.toMutableList().apply {
            if (showHomeFolder) add(
                DesktopFolderItem(
                    path = homeDir,
                    customName = "Home",
                    priority = 1
                )
            )
        }.sortedByDescending {
            it.priority
        }
    }
}

// todo
@Preview
@Composable
fun PreviewFolderView() = preview {
    FolderView(
        modifier = Modifier.fillMaxSize()
    )
}
