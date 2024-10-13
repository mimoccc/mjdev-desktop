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
import eu.mjdev.desktop.data.Category
import eu.mjdev.desktop.extensions.Compose.onLeftClick
import eu.mjdev.desktop.extensions.Compose.onMousePress
import eu.mjdev.desktop.extensions.Compose.onRightClick
import eu.mjdev.desktop.extensions.Modifier.circleBorder
import eu.mjdev.desktop.helpers.shape.DottedShape
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Composable
fun AppsMenuCategory(
    modifier: Modifier = Modifier,
    category: Category = Category.Empty,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    titleTextSize: TextUnit = 16.sp,
    showDivider: Boolean = true,
    dividerPadding: Dp = 48.dp,
    dividerColor: Color = Color.Black,
    onClick: (category: Category) -> Unit = {},
    onContextMenuClick: (category: Category) -> Unit = {}
) = withDesktopScope {
    val categoryName = remember(category) { category.name }
    val materialIcon = remember(categoryName) { iconSet.iconForName(categoryName) ?: "?".toInt() }
    Column(
        modifier = modifier.padding(vertical = 2.dp)
            .onMousePress {
                onLeftClick { onClick(category) }
                onRightClick { onContextMenuClick(category) }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
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
            TextAny(
                modifier = Modifier.padding(start = 4.dp).fillMaxWidth(),
                text = category.name,
                color = textColor,
                fontSize = titleTextSize,
                fontWeight = FontWeight.Bold
            )
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
fun AppsMenuCategoryPreview() = AppsMenuCategory()
