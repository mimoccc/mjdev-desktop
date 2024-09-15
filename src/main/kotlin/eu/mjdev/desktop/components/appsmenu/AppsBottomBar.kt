package eu.mjdev.desktop.components.appsmenu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@Composable
fun AppsBottomBar(
    api: DesktopProvider = LocalDesktop.current,
    modifier: Modifier = Modifier,
    iconBackgroundColor: Color = Color.White,
    iconColor: Color = Color.Black,
    backButtonVisible: Boolean = false,
    onBackClick: () -> Unit
) = Box(
    modifier = modifier.padding(8.dp)
) {
    Row(
        modifier = Modifier.align(Alignment.BottomStart)
    ) {
        if (backButtonVisible) {
            ShapedIcon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                iconColor = iconColor,
                iconBackgroundColor = iconBackgroundColor,
                onClick = onBackClick
            )
        }
    }
    Row(
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        // todo : center content if needed
    }
    Row(
        modifier = Modifier.align(Alignment.BottomEnd)
    ) {
        ShapedIcon(
            imageVector = Icons.Filled.RestartAlt,
            iconColor = iconColor,
            iconBackgroundColor = iconBackgroundColor,
        ) {
            api.restart()
        }
        ShapedIcon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            iconColor = iconColor,
            iconBackgroundColor = iconBackgroundColor,
        ) {
            api.logOut()
        }
        ShapedIcon(
            imageVector = Icons.Filled.PowerOff,
            iconColor = iconColor,
            iconBackgroundColor = iconBackgroundColor,
        ) {
            api.shutdown()
        }
    }
}