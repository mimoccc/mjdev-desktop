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
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.circleBorder
import org.mjdev.desktop.extensions.Modifier.onLeftClick
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.extensions.Modifier.onRightClick
import org.mjdev.desktop.helpers.shape.DottedShape

@Suppress("UNUSED_PARAMETER")
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
    onContextMenuClick: (category: Category) -> Unit = {},
    onTooltip: (item: Any?) -> Unit = {}
) = withDesktopContext {
    val categoryName = remember(category) { category.name }
    val materialIcon = remember(categoryName) { iconSet.iconForName(categoryName) ?: 0 }
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

//@Preview
@Suppress("unused")
@Composable
fun AppsMenuCategoryPreview() = preview {
    AppsMenuCategory(
        modifier = Modifier.padding(8.dp)
            .background(
                Color.SuperDarkGray,
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        category = Category("Audio")
    )
}
