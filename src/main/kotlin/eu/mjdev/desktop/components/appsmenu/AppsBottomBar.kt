package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.components.input.SearchField
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.icons.Icons
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Composable
fun AppsBottomBar(
    modifier: Modifier = Modifier,
    backButtonVisible: Boolean = false,
    onContextMenuClick: () -> Unit = {},
    onHideMenu: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) = withDesktopScope {
    val textState = rememberState("")
    Row(
        modifier = modifier.padding(8.dp)
    ) {
        if (backButtonVisible && textState.value.isEmpty()) {
            ShapedIcon(
                imageVector = Icons.BackArrow,
                iconColor = borderColor,
                iconBackgroundColor = iconsTintColor,
                onRightClick = onContextMenuClick,
                onClick = onBackClick
            )
        }
//        SearchField(
//            modifier = Modifier.fillMaxWidth().background(Color.Red),
//            textState = textState
//        )
        Row(
            modifier = Modifier.wrapContentWidth()
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
