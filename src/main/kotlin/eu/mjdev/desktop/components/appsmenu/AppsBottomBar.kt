package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.components.input.SearchFieldPassive
import eu.mjdev.desktop.extensions.Compose.clear
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.icons.Icons
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FunctionName")
@Composable
fun AppsBottomBar(
    modifier: Modifier = Modifier,
    backButtonVisible: Boolean = true,
    searchTextState: MutableState<String> = rememberState(""),
    onContextMenuClick: () -> Unit = {},
    onHideMenu: () -> Unit = {},
    onBackClick: () -> Unit = {}
) = withDesktopScope {
    TopAppBar(
        modifier = modifier.focusable(false),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = {
            SearchFieldPassive(
                modifier = Modifier.fillMaxWidth().focusable(true),
                textState = searchTextState,
                textColor = iconsTintColor,
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold
                ),
                onClearClick = { searchTextState.clear() }
            )
        },
        navigationIcon = {
            ShapedIcon(
                modifier = Modifier.focusable(false),
                visible = backButtonVisible && searchTextState.value.isEmpty(),
                imageVector = Icons.BackArrow,
                iconColor = borderColor,
                iconBackgroundColor = iconsTintColor,
                onRightClick = onContextMenuClick,
                onClick = onBackClick
            )
        },
        actions = {
            ShapedIcon(
                modifier = Modifier,
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
                modifier = Modifier,
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
                modifier = Modifier,
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
    )
}

@Preview
@Composable
fun AppsBottomBarPreview() = AppsBottomBar()
