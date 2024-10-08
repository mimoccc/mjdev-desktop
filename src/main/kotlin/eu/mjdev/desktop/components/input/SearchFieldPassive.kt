/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.input

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.material.Icon
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.icons.Icons

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchFieldPassive(
    modifier: Modifier = Modifier,
    textState: MutableState<String> = mutableStateOf(""),
    textColor: Color = Color.White,
    textStyle: TextStyle = TextStyle.Default,
    textSize:TextUnit = 18.sp,
    onClearClick: () -> Unit = {}
) = SelectableOutlineEditText(
    value = textState.value,
    modifier = modifier.focusable(false),
    enabled = false,
    singleLine = true,
    textStyle = textStyle,
    textSize=textSize,
    colors = TextFieldDefaults.outlinedTextFieldColors(
        backgroundColor = Color.Transparent,
        textColor = textColor,
        disabledTextColor = textColor,
        cursorColor = Color.Transparent,
        errorCursorColor = Color.Transparent,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        errorBorderColor = Color.Transparent
    ),
    trailingIcon = {
        if (textState.value.isNotEmpty()) {
            Icon(
                modifier = Modifier.padding(4.dp)
                    .size(24.dp)
                    .onClick { onClearClick() },
                imageVector = Icons.TextFieldClearText,
                tint = textColor,
                contentDescription = ""
            )
        }
    }
)