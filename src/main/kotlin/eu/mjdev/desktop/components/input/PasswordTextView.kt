package eu.mjdev.desktop.components.input

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasswordTextView(
    modifier: Modifier = Modifier,
    password: MutableState<String> = rememberSaveable { mutableStateOf("") },
    passwordVisible: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable () -> Unit = {},
    shape: Shape = RoundedCornerShape(8.dp),
    iconSize: Dp = 24.dp,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
    contentDescription: String = "Enter password",
    hidePasswordDescription: String = "Hide password",
    showPasswordDescription: String = "Show password",
    onDone: (password: String) -> Unit = {}
) {
    val iconVisible = remember {
        derivedStateOf {
            if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
        }
    }
    val iconSend = remember {
        derivedStateOf {
            if (password.value.isNotEmpty()) Icons.AutoMirrored.Filled.Send else null
        }
    }
    val description = remember {
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
        maxLines = 1,
        minLines = 1,
        shape = shape,
        colors = colors,
        placeholder = placeholder,
        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(iconSize).onClick { passwordVisible.value = !passwordVisible.value },
                    imageVector = iconVisible.value,
                    tint = colors.textColor(true).value,
                    contentDescription = description.value
                )
                if (iconSend.value != null) {
                    Icon(
                        modifier = Modifier.padding(4.dp).size(iconSize).onClick { onDone(password.value) },
                        imageVector = iconSend.value!!,
                        tint = colors.textColor(true).value,
                        contentDescription = contentDescription
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun PasswordTextViewPreview() = PasswordTextView()
