package org.mjdev.desktop.components.installer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.background.BackgroundImage
import org.mjdev.desktop.components.sliding.base.VisibilityState
import org.mjdev.desktop.components.sliding.base.VisibilityState.Companion.rememberVisibilityState
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync

@Composable
fun Installer(
    visibleState: VisibilityState = rememberVisibilityState(),
    onInstallClick: () -> Unit = {}
) = withDesktopContext {
    Box(
        modifier = Modifier.fillMaxSize().clickable {
            runAsync {
                visibleState.hide()
                onInstallClick()
            }
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