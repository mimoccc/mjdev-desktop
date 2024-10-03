package eu.mjdev.desktop.components.file

//import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import com.composables.core.*
//import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.data.DesktopFolderItem
import eu.mjdev.desktop.extensions.Modifier.conditional
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import java.io.File

// ScreencastHelper
// DefaultShellFolder
// ShellFolder
// Clipboard
// XBaseWindow
// XWM
// XWindow
// XWrapperBase
// ShellFolderManager
// X11GraphicsEnvironment
@Suppress("FunctionName", "SimplifyBooleanWithConstants")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderView(
    modifier: Modifier = Modifier,
    showHidden: Boolean = false,
    directoryFirst: Boolean = true,
    showHomeFolder: Boolean = false,
    iconSpacing: Dp = 2.dp,
    path: File? = File("/"),
    api: DesktopProvider = LocalDesktop.current,
    zIndexState: MutableState<Int> = remember { mutableStateOf(0) },
    orientation: Orientation = Orientation.Vertical,
    scrollState: ScrollState = rememberScrollState(),
//    scrollbarsState: ScrollAreaState = rememberScrollAreaState(scrollState)
) = withDesktopScope {
    val files by remember(path) {
        derivedStateOf {
            path?.list()
                ?.toList<String>()
                ?.sorted()
                ?.filter { s ->
                    (showHidden == true) || (!s.startsWith("."))
                }?.map { filePath ->
                    File(filePath)
                }?.sortedByDescending {
                    (directoryFirst == false) || (it.extension.isEmpty()) // todo isDirectory
                }?.map { path ->
                    DesktopFolderItem(path)
                }?.toMutableList()
                ?.apply {
                    if (showHomeFolder) add(
                        DesktopFolderItem(
                            path = api.homeDir,
                            customName = "Home",
                            priority = 1
                        )
                    )
                }?.sortedByDescending {
                    it.priority
                } ?: emptyList()
        }
    }
    val content: @Composable () -> Unit = {
        files.forEach { file ->
            FolderIcon(
                modifier = Modifier.padding(iconSpacing),
                path = file.path,
                customName = file.customName,
                zIndex = { zIndexState.value },
                onDragStart = {
                    zIndexState.value += 1
                }
            )
        }
    }
    Box(
        modifier = modifier.conditional(
            condition = orientation == Orientation.Horizontal,
            onTrue = {
                verticalScroll(
                    state = scrollState,
                    enabled = scrollState.canScrollForward
                )
            },
            onFalse = {
                horizontalScroll(
                    state = scrollState,
                    enabled = scrollState.canScrollForward
                )
            }
        )
    ) {
//    ScrollArea(
//        state = scrollbarsState
//    ) {
        if (orientation == Orientation.Horizontal) {
            FlowRow(
                modifier = Modifier.fillMaxSize(),
                overflow = FlowRowOverflow.Visible,
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Top,
                content = { content() }
            )
//            HorizontalScrollbar(
//                modifier = Modifier.align(Alignment.TopEnd)
//                    .fillMaxWidth()
//                    .height(4.dp)
//            ) {
//                Thumb(
//                    modifier = Modifier.background(Color.SuperDarkGray)
//                )
//            }
        } else {
            FlowColumn(
                modifier = Modifier.fillMaxSize(),
                overflow = FlowColumnOverflow.Visible,
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Top,
                content = { content() }
            )
//            VerticalScrollbar(
//                modifier = Modifier.align(Alignment.TopEnd)
//                    .fillMaxHeight()
//                    .width(4.dp)
//            ) {
//                Thumb(
//                    modifier = Modifier.background(Color.SuperDarkGray)
//                )
//            }
//        }
        }
    }
}

@Preview
@Composable
fun FolderViewPreview() = FolderView()
