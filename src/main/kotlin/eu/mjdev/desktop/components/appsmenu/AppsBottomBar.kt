package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Composable
fun AppsBottomBar(
    modifier: Modifier = Modifier,
    backButtonVisible: Boolean = false,
    onContextMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) = withDesktopScope {
    Box(
        modifier = modifier.padding(8.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            if (backButtonVisible) {
                ShapedIcon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    iconColor = borderColor,
                    iconBackgroundColor = iconsTintColor,
                    onRightClick = onContextMenuClick,
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
                iconColor = borderColor,
                iconBackgroundColor = iconsTintColor,
                onRightClick = onContextMenuClick,
                onClick = { api.restart() }
            )
            ShapedIcon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                iconColor = borderColor,
                iconBackgroundColor = iconsTintColor,
                onRightClick = onContextMenuClick,
                onClick = { api.logOut() }
            )
            ShapedIcon(
                imageVector = Icons.Filled.PowerOff,
                iconColor = borderColor,
                iconBackgroundColor = iconsTintColor,
                onRightClick = onContextMenuClick,
                onClick = { api.shutdown() }
            )
        }
    }
}

@Preview
@Composable
fun AppsBottomBarPreview() = AppsBottomBar()