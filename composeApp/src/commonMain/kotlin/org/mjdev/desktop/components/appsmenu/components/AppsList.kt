/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.appsmenu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
    items: List<Any> = listOf(),
    listState: LazyListState = rememberForeverLazyListState("AppsMenu"),
    onCategoryClick: DesktopContextScope.(Category) -> Unit = {},
    onCategoryContextMenuClick: DesktopContextScope.(Category) -> Unit = {},
    onAppClick: DesktopContextScope.(IApp) -> Unit = { app -> runAsync { app.start() } },
    onAppContextMenuClick: DesktopContextScope.(IApp) -> Unit = {},
    onTooltip: (item: Any?) -> Unit = {},
) = withDesktopContext {
    val state = if (category == null) listState else rememberLazyListState()
    val clickEnabled = remember { mutableStateOf(true) }
    LazyColumn(
        // drag to scroll - mouse drag on desktop, touch drag on android;
        // drag down reveals earlier content, so the delta is inverted
        modifier = modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                runAsync {
                    state.scrollBy(-delta)
                }
            },
            onDragStarted = { clickEnabled.value = false },
            onDragStopped = { clickEnabled.value = true },
        ),
        state = state,
    ) {
        when (items.firstOrNull()) {
            is Category -> {
                itemsIndexed(items) { idx, item ->
                    AppsMenuCategory(
                        category = item as Category,
                        showDivider = (items.size - 1) > idx,
                        dividerColor = textColor.alpha(0.3f),
                        onClick = { if (clickEnabled.value) onCategoryClick(item) },
                        onContextMenuClick = { if (clickEnabled.value) onCategoryContextMenuClick(item) },
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
                            if (clickEnabled.value) onAppClick(item)
                        },
                        onContextMenuClick = {
                            if (clickEnabled.value) onAppContextMenuClick(item)
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
fun PreviewAppsList() = preview(320, 640) {
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