package eu.mjdev.desktop.components.info

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.background.BackgroundImage
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow

@Suppress("FunctionName")
@Composable
fun InfoWindow() = withDesktopScope {
    var visible by rememberState(true)
    ChromeWindow(
        visible = visible,
        size = DpSize(808.dp, 608.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().clickable {
                visible = false
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                BackgroundImage(
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(200.dp)
                        .background(iconsTintColor)
                        .align(Alignment.BottomCenter)
                ) {
                    // todo
                }
            }
        }
    }
}

@Preview
@Composable
fun InfoWindowPreview() = InfoWindow()