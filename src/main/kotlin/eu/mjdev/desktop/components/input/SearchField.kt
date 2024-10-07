/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    textState: MutableState<String> = rememberSaveable { mutableStateOf("") },
) = SelectableOutlineEditText(
    value = textState.value,
    onValueChange = { v: String -> textState.value = v },
    modifier = modifier
)