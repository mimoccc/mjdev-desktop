package org.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.jetbrains.compose.ui.tooling.preview.Preview

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
fun DesktopPanelLanguagePreview() = preview {
    DesktopPanelLanguage()
}
