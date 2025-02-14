package org.mjdev.desktop.components.installer

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.windows.ChromeWindow

@Suppress("FunctionName")
@Composable
fun InstallerWindow(
    visibleState: VisibilityState = rememberVisibilityState()
) = withDesktopContext {
    ChromeWindow(
        visible = visibleState.isVisible,
        size = DpSize(808.dp, 608.dp),
        position = DpOffset.Zero
    ) {
        Installer(
            visibleState = visibleState
        )
    }
}

@Suppress("unused")
@Preview
@Composable
fun InfoWindowPreview() = preview {
    InstallerWindow()
}
