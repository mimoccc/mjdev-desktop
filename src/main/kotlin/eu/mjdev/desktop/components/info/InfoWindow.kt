package eu.mjdev.desktop.components.info

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.background.BackgroundImage
import eu.mjdev.desktop.components.button.ButtonPrimary
import eu.mjdev.desktop.components.sliding.base.VisibilityState
import eu.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import eu.mjdev.desktop.components.text.AutoResizeText
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow

@Suppress("FunctionName")
@Composable
fun InfoWindow(
    state: VisibilityState = rememberVisibilityState(),
    showInstallWindow: () -> Unit = {}
) = withDesktopScope {
    ChromeWindow(
        visible = state.isVisible,
        size = DpSize(808.dp, 608.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().clickable {
                state.hide()
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.BottomStart
            ) {
                BackgroundImage(
                    modifier = Modifier.fillMaxSize()
                )
                AutoResizeText(
                    modifier = Modifier.padding(bottom = 128.dp, start = 16.dp)
                        .height(32.dp),
                    text = "Welcome to mjdev desktop",
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                )
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(128.dp)
                        .background(iconsTintColor)
                        .align(Alignment.BottomCenter),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomEnd),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ButtonPrimary(
                            text = "install mjdev desktop",
                            visible = !api.isInstalled,
                            onClick = showInstallWindow,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun InfoWindowPreview() = preview {
    InfoWindow()
}
