package org.psippr.desktop.components.info

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.psippr.desktop.components.sliding.base.VisibilityState
import org.psippr.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.windows.ChromeWindow

@Suppress("FunctionName")
@Composable
fun InfoWindow(
    visibleState: VisibilityState = rememberVisibilityState(),
    showInstallWindow: () -> Unit = {},
) = withDesktopContext {
    ChromeWindow(
        visible = visibleState.isVisible,
        size = DpSize(808.dp, 608.dp),
        position = DpOffset.Zero,
    ) {
        Info(
            visibleState = visibleState,
            onInstallClick = showInstallWindow,
        )
    }
}

@Suppress("unused")
@Preview
@Composable
fun PreviewInfoWindow() = preview {
    InfoWindow()
}
