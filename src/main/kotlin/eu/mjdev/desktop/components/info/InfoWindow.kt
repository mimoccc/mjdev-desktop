package eu.mjdev.desktop.components.info

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import eu.mjdev.desktop.components.background.BackgroundImage
import eu.mjdev.desktop.extensions.Modifier.dropShadow
import eu.mjdev.desktop.extensions.Modifier.rectShadow
import eu.mjdev.desktop.provider.DesktopProvider.Companion.withDesktopScope
import eu.mjdev.desktop.windows.ChromeWindow

@Suppress("FunctionName")
@Preview
@Composable
fun InfoWindow() = withDesktopScope {
    ChromeWindow(
        visible = true,
        size = DpSize(808.dp, 608.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
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