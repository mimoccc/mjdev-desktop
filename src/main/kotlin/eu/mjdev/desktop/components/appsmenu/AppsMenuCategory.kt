package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.mouseClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.fonticon.FontIcon
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.data.Category
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Suppress("DEPRECATION")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppsMenuCategory(
    modifier: Modifier = Modifier,
    category: Category = Category.Empty,
    iconSize: DpSize = DpSize(32.dp, 32.dp),
    onClick: (category: Category) -> Unit = {},
    onContextMenuClick: (category: Category) -> Unit = {}
) = withDesktopScope {
    Row(
        modifier = modifier.mouseClickable {
            if (buttons.isSecondaryPressed) {
                onContextMenuClick(category)
            } else {
                onClick(category)
            }
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val categoryName = remember(category) { category.name }
        val materialIcon = remember(categoryName) { iconSet.iconForName(categoryName) ?: "?".toInt() }
        FontIcon(
            iconId = materialIcon,
            iconSize = iconSize,
            iconColor = textColor,
            iconBackgroundColor = backgroundColor,
            outerPadding = PaddingValues(2.dp),
            innerPadding = PaddingValues(0.dp),
            onClick = { onClick(category) },
            onRightClick = { onContextMenuClick(category) }
        )
        TextAny(
            modifier = Modifier.padding(start = 4.dp).fillMaxWidth(),
            text = category.name,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun AppsMenuCategoryPreview() = AppsMenuCategory()
