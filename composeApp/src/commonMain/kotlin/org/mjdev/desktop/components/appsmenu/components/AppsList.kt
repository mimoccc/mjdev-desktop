/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.appsmenu.components

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
import org.mjdev.desktop.data.Category
import org.mjdev.desktop.extensions.Colors.SuperDarkGray
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.helpers.compose.rememberForeverLazyListState
import org.mjdev.desktop.context.DesktopContextScope
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.interfaces.IApp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppsList(
    modifier: Modifier = Modifier,
    category: Category? = null,
    listState: LazyListState = rememberForeverLazyListState("AppsMenu"),
    onCategoryClick: DesktopContextScope.(Category) -> Unit = {},
    onCategoryContextMenuClick: DesktopContextScope.(Category) -> Unit = {},
    onAppClick: DesktopContextScope.(IApp) -> Unit = { app -> runAsync { app.start() } },
    onAppContextMenuClick: DesktopContextScope.(IApp) -> Unit = {},
    items: List<Any> = listOf(),
    onTooltip: (item: Any?) -> Unit = {}
) = withDesktopContext {
    LazyColumn(
        modifier = modifier,
        state = if (category == null) listState else rememberLazyListState(),
    ) {
        when (items.firstOrNull()) {
            is Category -> {
                itemsIndexed(items) { idx, item ->
                    AppsMenuCategory(
                        category = item as Category,
                        showDivider = (items.size - 1) > idx,
                        dividerColor = textColor.alpha(0.3f),
                        onClick = { onCategoryClick(item) },
                        onContextMenuClick = { onCategoryContextMenuClick(item) },
                        onTooltip = onTooltip
                    )
                }
            }

            is IApp -> {
                itemsIndexed(items) { idx, item ->
                    AppsMenuApp(
                        app = item as IApp,
                        showDivider = (items.size - 1) > idx,
                        dividerColor = textColor.alpha(0.3f),
                        onClick = {
                            onAppClick(item)
                        },
                        onContextMenuClick = {
                            onAppContextMenuClick(item)
                        },
                        onTooltip = onTooltip
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AppsListPreview() = preview(320, 640) {
    AppsList(
        modifier = Modifier
            .background(
                Color.SuperDarkGray,
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        items = listOf(
//            App.Test,
//            App.Test,
//            App.Test
        )
    )
}