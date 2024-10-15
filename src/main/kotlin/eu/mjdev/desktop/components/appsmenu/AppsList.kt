/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.appsmenu

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.data.App
import eu.mjdev.desktop.data.Category
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.helpers.compose.rememberForeverLazyListState
import eu.mjdev.desktop.provider.DesktopScope
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Composable
fun AppsList(
    modifier: Modifier = Modifier,
    category: String = "",
    listState: LazyListState = rememberForeverLazyListState("AppsMenu"),
    onCategoryClick: DesktopScope.(Category) -> Unit = {},
    onCategoryContextMenuClick: DesktopScope.(Category) -> Unit = {},
    onAppClick: DesktopScope.(App) -> Unit = {},
    onAppContextMenuClick: DesktopScope.(App) -> Unit = {},
    items: List<Any> = listOf()
) = withDesktopScope {
    LazyColumn(
        modifier = modifier,
        state = if (category.isEmpty()) listState else rememberLazyListState(),
    ) {
        when (items.firstOrNull()) {
            is Category -> {
                itemsIndexed(items) { idx, item ->
                    AppsMenuCategory(
                        category = item as Category,
                        showDivider = (items.size - 1) > idx,
                        dividerColor = textColor.alpha(0.3f),
                        onClick = { onCategoryClick(item) },
                        onContextMenuClick = { onCategoryContextMenuClick(item) }
                    )
                }
            }

            is App -> {
                itemsIndexed(items) { idx, item ->
                    AppsMenuApp(
                        app = item as App,
                        showDivider = (items.size - 1) > idx,
                        dividerColor = textColor.alpha(0.3f),
                        onClick = {
                            onAppClick(item)
                        },
                        onContextMenuClick = {
                            onAppContextMenuClick(item)
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AppsListPreview() {
    AppsList(
        modifier = Modifier.padding(8.dp)
            .background(
                Color.Gray,
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        items = listOf(App.Test, App.Test, App.Test)
    )
}