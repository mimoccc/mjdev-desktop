/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.list

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun <T> ExpandableLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) {
            Arrangement.Top
        } else {
            Arrangement.Bottom
        },
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    items: List<T>,
    expandStates: MutableMap<Int, MutableState<Boolean>> = remember { mutableMapOf() },
    createItem: @Composable (idx: Int, item: T, expanded: MutableState<Boolean>) -> Unit,
) = LazyColumn(
    modifier,
    state,
    contentPadding,
    reverseLayout,
    verticalArrangement,
    horizontalAlignment,
    flingBehavior,
    userScrollEnabled,
) {
    itemsIndexed(
        items = items,
        key = { _, item -> item.hashCode() },
    ) { idx, item ->
        val hash = item.hashCode()
        if (!expandStates.containsKey(hash)) {
            expandStates[hash] = mutableStateOf(false)
        }
        ExpandableListItem(
            modifier = Modifier.fillMaxWidth(),
            expanded = expandStates[hash]!!,
        ) { expandedState ->
            createItem(idx, item, expandedState)
        }
    }
}

// todo preview
