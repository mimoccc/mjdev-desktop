package eu.mjdev.desktop.components.file

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowColumnOverflow
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.data.DesktopFolderItem
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
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
    path: File? = File("/"),
    api: DesktopProvider = LocalDesktop.current,
    files: List<DesktopFolderItem> = remember(path) {
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
    },
    zIndexState: MutableState<Int> = remember { mutableStateOf(files.size) },
    orientation: Orientation = Orientation.Vertical
) = Box(
    modifier = modifier.verticalScroll(rememberScrollState())
) {
    val content: @Composable () -> Unit = {
        files.forEach { file ->
            FolderIcon(
                path = file.path,
                customName=file.customName,
                zIndex = { zIndexState.value },
                onDragStart = {
                    zIndexState.value += 1
                }
            )
        }
    }
    if (orientation == Orientation.Horizontal) {
        FlowRow(
            modifier = Modifier.fillMaxSize(),
            overflow = FlowRowOverflow.Visible,
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Top,
            content = { content() }
        )
    } else {
        FlowColumn(
            modifier = Modifier.fillMaxSize(),
            overflow = FlowColumnOverflow.Visible,
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Top,
            content = { content() }
        )
    }
}

@Preview
@Composable
fun FolderViewPreview() = FolderView()