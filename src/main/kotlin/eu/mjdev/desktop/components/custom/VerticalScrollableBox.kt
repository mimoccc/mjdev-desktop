/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.extensions.Compose.isLandscapeMode
import eu.mjdev.desktop.extensions.Compose.verticalTouchScrollable

@Composable
fun VerticalScrollableBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = isLandscapeMode(),
    state: LazyListState = rememberLazyListState(),
    content: @Composable BoxScope.() -> Unit = {}
) = Box(
    modifier = modifier.verticalTouchScrollable(state),
    contentAlignment = contentAlignment,
    propagateMinConstraints = propagateMinConstraints,
) {
    content.invoke(this)
}

@Preview
@Composable
fun VerticalScrollableBoxPreview() = VerticalScrollableBox()
