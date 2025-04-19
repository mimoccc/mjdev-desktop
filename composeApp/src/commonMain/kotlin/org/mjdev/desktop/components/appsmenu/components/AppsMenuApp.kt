package org.mjdev.desktop.components.appsmenu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.fonticon.FontIcon
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync
import org.mjdev.desktop.extensions.Modifier.circleBorder
import org.mjdev.desktop.extensions.Modifier.onLeftClick
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.extensions.Modifier.onRightClick
import org.mjdev.desktop.helpers.shape.DottedShape
import org.mjdev.desktop.interfaces.IApp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("FunctionName", "UNUSED_PARAMETER")
@Composable
fun AppsMenuApp(
    modifier: Modifier = Modifier,
    app: IApp? = null,
    icon: String? = null,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    titleTextSize: TextUnit = 16.sp,
    commentTextSize: TextUnit = 12.sp,
    showDivider: Boolean = true,
    dividerPadding: Dp = 48.dp,
    dividerColor: Color = Color.Black,
    onContextMenuClick: () -> Unit = {},
    onClick: () -> Unit = { runAsync { app?.start() } },
    onTooltip: (item: Any?) -> Unit = {}
) = withDesktopContext {
    val appIconName = remember(app, icon) { app?.name ?: icon }
    val materialIcon = remember(appIconName) { iconSet.iconForName(appIconName) ?: 0 }
    Column(
        modifier = modifier.onMousePress {
            onLeftClick { onClick() }
            onRightClick { onContextMenuClick() }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FontIcon(
                modifier = Modifier.circleBorder(1.dp, textColor),
                iconId = materialIcon,
                iconSize = iconSize,
                iconColor = textColor,
                iconBackgroundColor = backgroundColor,
                outerPadding = PaddingValues(2.dp),
                innerPadding = PaddingValues(0.dp),
            )
            Column(
                Modifier.padding(4.dp).fillMaxWidth()
            ) {
                TextAny(
                    text = (app?.name ?: app?.fullAppName.orEmpty()).ifEmpty { "-" },
                    color = textColor,
                    singleLine = true,
                    fontSize = titleTextSize,
                    fontWeight = FontWeight.Bold
                )
                TextAny(
                    text = (app?.comment.orEmpty()).ifEmpty { "-" },
                    color = textColor,
                    singleLine = true,
                    fontSize = commentTextSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (showDivider) {
            Divider(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = dividerPadding)
                    .height(1.dp)
                    .background(
                        color = dividerColor,
                        shape = DottedShape(5.dp)
                    ),
                color = Color.Transparent
            )
        }
    }
}

@Preview
@Composable
fun AppsMenuAppPreview() = preview {
    AppsMenuApp(
        modifier = Modifier.padding(8.dp)
            .background(
                Color.SuperDarkGray,
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        app = null // App.Test
    )
}
