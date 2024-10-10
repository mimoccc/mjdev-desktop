package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.appbar.AppBar
import eu.mjdev.desktop.components.icon.ShapedIcon
import eu.mjdev.desktop.components.input.SearchFieldPassive
import eu.mjdev.desktop.extensions.Compose.clear
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.icons.Icons
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Composable
fun AppsBottomBar(
    modifier: Modifier = Modifier,
    backButtonVisible: Boolean = true,
    searchTextState: MutableState<String> = rememberState(""),
    onContextMenuClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) = withDesktopScope {
    AppBar(
        modifier = modifier.focusable(false),
        title = {
            SearchFieldPassive(
                modifier = Modifier.fillMaxWidth().focusable(true),
                textState = searchTextState,
                textColor = iconsTintColor,
                textSize = 20.sp,
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold
                ),
                onClearClick = { searchTextState.clear() }
            )
        },
        icon = {
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
            AppActions(
                onActionClick = onActionClick
            )
        }
    )
}

@Preview
@Composable
fun AppsBottomBarPreview() = AppsBottomBar()
