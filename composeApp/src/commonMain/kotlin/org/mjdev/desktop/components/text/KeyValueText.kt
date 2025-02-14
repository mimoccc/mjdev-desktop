/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Text.textFrom
import org.mjdev.desktop.icons.visibility.VisibilityOff
import org.mjdev.desktop.icons.visibility.VisibilityOn
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Modifier.onMousePress

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyValueText(
    modifier: Modifier = Modifier,
    key: Any,
    value: Any,
    editable: Boolean = false,
    onValueChange: (String) -> Unit = {},
    iconSize: Dp = 24.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    keyboardType: KeyboardType = KeyboardType.Text,
    keyTranscription: (text: Any) -> Any = { text -> text },
    valueTranscription: (text: Any) -> Any = { text -> text },
    textPadding: PaddingValues = PaddingValues(horizontal = 8.dp)
) = withDesktopContext {
    var textValue by rememberSaveable { mutableStateOf(textFrom(valueTranscription(value))) }
    var passwordVisible by rememberSaveable { mutableStateOf(keyboardType != KeyboardType.Password) }
    val isPassword by rememberSaveable { mutableStateOf(keyboardType == KeyboardType.Password) }
    val iconVisible by rememberSaveable {
        derivedStateOf {
            // todo ?
            if (passwordVisible) VisibilityOn else VisibilityOff
        }
    }
    var isFocused by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier
    ) {
        TextAny(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = keyTranscription(key),
            color = textColor
        )
        Spacer(
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = textValue,
            shape = shape,
            modifier = Modifier.align(Alignment.CenterVertically)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused || focusState.hasFocus
                    if (isPassword && !isFocused) passwordVisible = false
                },
            textStyle = LocalTextStyle.current.copy(
                letterSpacing = 0.sp
            ),
            readOnly = !editable,
            singleLine = true,
            onValueChange = { text ->
                textValue = text
                onValueChange(text)
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = if (isFocused) focusedTextBackgroundColor.alpha(0.3f) else Color.Transparent,
                textColor = textColor,
                disabledTextColor = textColor,
                cursorColor = textColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = {
                if (isPassword) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            modifier = Modifier.size(iconSize)
                                .onMousePress { // todo on click?
                                    passwordVisible = !passwordVisible
                                },
                            imageVector = iconVisible,
                            contentDescription = "",
                            tint = textColor,
                        )
                    }
                }
            },
            textPadding = textPadding
        )
    }
}