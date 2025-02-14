package org.mjdev.desktop.components.info

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.background.BackgroundImage
import org.mjdev.desktop.components.button.ButtonPrimary
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.components.text.AutoResizeText
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync

@Composable
fun Info(
    visibleState: VisibilityState = rememberVisibilityState(),
    onInstallClick: () -> Unit = {}
) = withDesktopContext {
    Box(
        modifier = Modifier.fillMaxSize().clickable {
            runAsync {
                visibleState.hide()
            }
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
                        visible = !context.isInstalled,
                        onClick = onInstallClick,
                    )
                }
            }
        }
    }
}