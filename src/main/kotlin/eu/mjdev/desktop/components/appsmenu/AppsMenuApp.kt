package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import eu.mjdev.desktop.components.fonticon.FontIcon
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.extensions.Compose.onLeftClick
import eu.mjdev.desktop.extensions.Compose.onMousePress
import eu.mjdev.desktop.extensions.Compose.onRightClick
import eu.mjdev.desktop.extensions.Modifier.circleBorder
import eu.mjdev.desktop.helpers.shape.DottedShape
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("FunctionName")
@Composable
fun AppsMenuApp(
    modifier: Modifier = Modifier,
    app: App? = null,
    icon: String? = null,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    titleTextSize: TextUnit = 16.sp,
    commentTextSize: TextUnit = 12.sp,
    showDivider: Boolean = true,
    dividerPadding: Dp = 48.dp,
    dividerColor: Color = Color.Black,
    onContextMenuClick: () -> Unit = {},
    onClick: () -> Unit = {},
) = withDesktopScope {
    val appIconName = remember(app, icon) { app?.name ?: icon }
    val materialIcon = remember(appIconName) { iconSet.iconForName(appIconName) ?: "?".toInt() }
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
                modifier = Modifier.circleBorder(1.dp, iconsTintColor),
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
                    text = app?.name ?: app?.fullAppName ?: "",
                    color = textColor,
                    singleLine = true,
                    fontSize = titleTextSize,
                    fontWeight = FontWeight.Bold
                )
                TextAny(
                    text = app?.comment ?: "-",
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
fun AppsMenuAppPreview() = AppsMenuApp()
