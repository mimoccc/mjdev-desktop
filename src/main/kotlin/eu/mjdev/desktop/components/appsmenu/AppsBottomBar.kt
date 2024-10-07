package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.components.input.SearchField
import eu.mjdev.desktop.icons.Icons
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Composable
fun AppsBottomBar(
    modifier: Modifier = Modifier,
    backButtonVisible: Boolean = false,
    onContextMenuClick: () -> Unit = {},
    onHideMenu: () -> Unit = {},
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
                    imageVector = Icons.BackArrow,
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
            SearchField(
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ShapedIcon(
                imageVector = Icons.RestartComputer,
                iconColor = borderColor,
                iconBackgroundColor = iconsTintColor,
                onRightClick = onContextMenuClick,
                onClick = {
                    onHideMenu()
                    // todo : dialog
                    api.restart()
                }
            )
            ShapedIcon(
                imageVector = Icons.PowerOffComputer,
                iconColor = borderColor,
                iconBackgroundColor = iconsTintColor,
                onRightClick = onContextMenuClick,
                onClick = {
                    onHideMenu()
                    // todo : dialog
                    api.suspend()
                }
            )
            ShapedIcon(
                imageVector = Icons.LogOutUser,
                iconColor = borderColor,
                iconBackgroundColor = iconsTintColor,
                onRightClick = onContextMenuClick,
                onClick = {
                    onHideMenu()
                    // todo : dialog
                    api.logOut()
                }
            )
        }
    }
}

@Preview
@Composable
fun AppsBottomBarPreview() = AppsBottomBar()
