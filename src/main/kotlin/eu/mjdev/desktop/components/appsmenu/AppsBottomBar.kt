package eu.mjdev.desktop.components.appsmenu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.icon.ShapedIcon
import kotlin.system.exitProcess

@Composable
fun AppsBottomBar(
    modifier: Modifier = Modifier
) = Box(
    modifier = modifier.padding(8.dp)
) {
    Row(
        modifier = Modifier.align(Alignment.BottomStart)
    ) {

    }

    Row(
        modifier = Modifier.align(Alignment.BottomEnd)
    ) {
        ShapedIcon(
            imageVector = Icons.Filled.RestartAlt,
            iconBackgroundColor = Color.White.copy(alpha = 0.7f),
        ) {
            exitProcess(0)
        }
        ShapedIcon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            iconBackgroundColor = Color.White.copy(alpha = 0.7f),
        ) {
            exitProcess(0)
        }
        ShapedIcon(
            imageVector = Icons.Filled.PowerOff,
            iconBackgroundColor = Color.White.copy(alpha = 0.7f),
        ) {
            exitProcess(0)
        }
    }
}