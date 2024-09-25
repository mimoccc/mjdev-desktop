package eu.mjdev.desktop.components.desktoppanel.applets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.components.desktoppanel.DesktopPanelText
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@Suppress("FunctionName")
@Preview
@Composable
fun DesktopPanelLanguage(
    modifier: Modifier = Modifier,
    api: DesktopProvider = LocalDesktop.current,
    onTooltip: (item: Any?) -> Unit = {},
    onClick: () -> Unit = {},
) {
    DesktopPanelText(
        modifier = modifier,
        text = api.appsProvider.currentLocale.country,
        backgroundHover = Color.White.copy(alpha = 0.4f),
        onToolTip = onTooltip,
        onClick = onClick
    )
}