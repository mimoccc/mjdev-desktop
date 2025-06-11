package org.mjdev.desktop.components.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.onMousePress
import org.mjdev.desktop.icons.text.Send
import org.mjdev.desktop.icons.visibility.VisibilityOff
import org.mjdev.desktop.icons.visibility.VisibilityOn
import org.jetbrains.compose.ui.tooling.preview.Preview

// todo check colors
@Suppress("FunctionName")
@Composable
fun PasswordTextView(
    modifier: Modifier = Modifier,
    password: MutableState<String> = rememberSaveable { mutableStateOf("") },
    passwordVisible: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    backgroundColor: Color = Color.White.copy(0.5f),
    textColor: Color = Color.White,
    focusedBorderColor: Color = Color.White,
    unfocusedBorderColor: Color = Color.Transparent,
    cursorColor: Color = Color.White,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable () -> Unit = {},
    shape: Shape = RoundedCornerShape(8.dp),
    iconSize: Dp = 24.dp,
    contentDescription: String = "Enter password",
    hidePasswordDescription: String = "Hide password",
    showPasswordDescription: String = "Show password",
    onDone: (password: String) -> Unit = {}
) {
    val iconVisible by remember {
        derivedStateOf {
            if (passwordVisible.value) VisibilityOn
            else VisibilityOff
        }
    }
    val iconSend by remember {
        derivedStateOf {
            if (password.value.isNotEmpty()) Send else null
        }
    }
    val description by remember {
        derivedStateOf {
            if (passwordVisible.value) hidePasswordDescription else showPasswordDescription
        }
    }
    OutlinedTextField(
        value = password.value,
        onValueChange = { v: String -> password.value = v },
        modifier = modifier,
        enabled = true,
        readOnly = false,
        label = label,
        singleLine = true,
        shape = shape,
        colors = TextFieldDefaults.textFieldColors(
            cursorColor = cursorColor,
            backgroundColor = backgroundColor,
            focusedIndicatorColor = focusedBorderColor,
            unfocusedIndicatorColor = unfocusedBorderColor,
            textColor = textColor
        ),
        placeholder = placeholder,
        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(iconSize)
                        .onMousePress { passwordVisible.value = !passwordVisible.value },
                    imageVector = iconVisible,
                    tint = textColor, //colors.textColor(true).value,
                    contentDescription = description
                )
                if (iconSend != null) {
                    Icon(
                        modifier = Modifier.padding(4.dp).size(iconSize)
                            .onMousePress { onDone(password.value) },
                        imageVector = iconSend!!,
                        tint = textColor, //colors.textColor(true).value,
                        contentDescription = contentDescription
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun PreviewPasswordTextView() = preview {
    PasswordTextView(
        password = mutableStateOf("test")
    )
}
