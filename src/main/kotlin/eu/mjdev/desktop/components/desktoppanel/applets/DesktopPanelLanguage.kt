package eu.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.desktoppanel.DesktopPanelText
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Preview
@Composable
fun DesktopPanelLanguage(
    modifier: Modifier = Modifier,
    onTooltip: (item: Any?) -> Unit = {},
    onClick: () -> Unit = {},
) = withDesktopScope {
    DesktopPanelText(
        modifier = modifier,
        text = currentLocale.country,
        onTooltip = onTooltip,
        onClick = onClick,
    )
}

@Preview
@Composable
fun DesktopPanelLanguagePreview() = DesktopPanelLanguage()
