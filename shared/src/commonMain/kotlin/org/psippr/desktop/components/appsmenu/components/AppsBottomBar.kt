package org.psippr.desktop.components.appsmenu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.psippr.desktop.components.appbar.AppBar
import org.psippr.desktop.components.icon.ShapedIcon
import org.psippr.desktop.components.input.SearchFieldPassive
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.SuperDarkGray
import org.psippr.desktop.extensions.Compose.preview
import org.psippr.desktop.extensions.MutableStateExt.clear
import org.psippr.desktop.extensions.MutableStateExt.rememberState
import org.psippr.desktop.icons.arrow.ArrowBack

@Suppress("FunctionName")
@Composable
fun AppsBottomBar(
    modifier: Modifier = Modifier,
    backButtonVisible: Boolean = true,
    searchTextState: MutableState<String> = rememberState(""),
    onContextMenuClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onTooltip: (item: Any?) -> Unit = {},
) = withDesktopContext {
    AppBar(
        modifier = modifier.focusable(false),
        title = {
            SearchFieldPassive(
                modifier =
                    Modifier
                        .padding(
                            start = 8.dp,
                            end = 8.dp,
                        ).fillMaxWidth()
                        .focusable(true),
                textState = searchTextState,
                textColor = iconsTintColor,
                textSize = 20.sp,
                textStyle =
                    TextStyle(
                        fontWeight = FontWeight.Bold,
                    ),
                backgroundColor = Color.Transparent,
                onClearClick = { searchTextState.clear() },
                onTooltip = onTooltip,
            )
        },
        icon = {
            ShapedIcon(
                modifier = Modifier.focusable(false),
                visible = backButtonVisible && searchTextState.value.isEmpty(),
                imageVector = ArrowBack,
                iconColor = borderColor,
                iconBackgroundColor = iconsTintColor,
                onRightClick = onContextMenuClick,
                onClick = onBackClick,
                onTooltip = onTooltip,
            )
        },
        actions = {
            AppsMenuActions(
                onActionClick = onActionClick,
                onTooltip = onTooltip,
            )
        },
    )
}

@Preview
@Composable
fun PreviewAppsBottomBar() = preview {
    AppsBottomBar(
        modifier = Modifier.background(Color.SuperDarkGray),
    )
}
