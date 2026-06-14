/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.list

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.extensions.MutableStateExt.toggle

@Composable
fun ExpandableListItem(
    modifier: Modifier = Modifier,
    expanded: MutableState<Boolean> = mutableStateOf(false),
    content: @Composable (
        expandedState: MutableState<Boolean>,
    ) -> Unit,
) = Box(
    modifier = modifier,
) {
    content(expanded)
}

// todo preview
