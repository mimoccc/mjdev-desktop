/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.custom

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import eu.mjdev.desktop.extensions.Compose.isLandscapeMode

@Composable
fun VerticalScrollableBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = isLandscapeMode(),
    state: LazyListState? = rememberLazyListState(),
    content: @Composable BoxScope.() -> Unit = {}
) = Box(
    modifier = modifier
        .pointerInput(Unit) {
            detectVerticalDragGestures { _, dragAmount ->
                state?.dispatchRawDelta(-dragAmount)
            }
        },
    contentAlignment = contentAlignment,
    propagateMinConstraints = propagateMinConstraints,
) {
    content.invoke(this)
}
