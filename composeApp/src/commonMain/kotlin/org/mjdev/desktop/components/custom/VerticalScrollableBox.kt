/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.custom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Compose.verticalTouchScrollable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun VerticalScrollableBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false, // isLandscapeMode(),
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
fun VerticalScrollableBoxPreview() = preview {
    VerticalScrollableBox()
}
