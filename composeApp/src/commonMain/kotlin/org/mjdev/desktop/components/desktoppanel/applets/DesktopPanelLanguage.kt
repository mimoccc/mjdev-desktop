package org.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview

@Suppress("FunctionName")
@Composable
fun DesktopPanelLanguage(
    modifier: Modifier = Modifier,
    onTooltip: (item: Any?) -> Unit = {},
    onClick: () -> Unit = {},
) = withDesktopContext {
    DesktopPanelText(
        modifier = modifier,
        text = currentLocale.country,
        onTooltip = onTooltip,
        onClick = onClick,
    )
}

@Preview
@Composable
fun vDesktopPanelLanguage() =
    preview {
        DesktopPanelLanguage()
    }
