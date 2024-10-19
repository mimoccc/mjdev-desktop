package eu.mjdev.desktop.components.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import eu.mjdev.desktop.components.desktop.widgets.MemoryChart
import eu.mjdev.desktop.components.file.FolderView
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.rememberCalculated
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindowState
import eu.mjdev.desktop.windows.ChromeWindowState.Companion.rememberChromeWindowState

@Suppress("FunctionName")
@Preview
@Composable
fun Desktop(
    modifier: Modifier = Modifier,
    panelState: ChromeWindowState = rememberChromeWindowState(),
) = withDesktopScope {
    val bottomPadding by rememberCalculated(panelState.enabled, panelState.height) {
        if (panelState.enabled) 0.dp else max(0.dp, panelState.height)
    }
    BoxWithConstraints(
        modifier = modifier.padding(16.dp)
    ) {
        Box(
            modifier = Modifier.size(
                constraints.maxWidth.dp,
                constraints.maxHeight.dp
            ).padding(
                bottom = bottomPadding
            )
        ) {
            FolderView(
                modifier = Modifier.fillMaxSize(),
                path = api.currentUser.userDirs.desktopDirectory,
                showHomeFolder = true,
//                orientation = Orientation.Vertical,
            )
            MemoryChart(
                modifier = Modifier.size(350.dp, 300.dp)
                    .align(Alignment.BottomEnd)
            )
        }
//        ComposeWebView(
//            modifier = Modifier.size(640.dp, 480.dp)
//                .align(Alignment.Center)
//        )
    }
}

@Preview
@Composable
fun DesktopPreview() = preview {
    Desktop()
}
