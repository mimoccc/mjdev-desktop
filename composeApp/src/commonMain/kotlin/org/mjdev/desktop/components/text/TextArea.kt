package org.mjdev.desktop.components.text

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.input.SelectableOutlineEditText
import org.mjdev.desktop.extensions.MutableStateExt.rememberComputed
import org.mjdev.desktop.icons.system.ContentCopy
import org.mjdev.desktop.icons.text.Send
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Modifier.onMousePress

@Composable
fun TextArea(
    modifier: Modifier = Modifier,
    textState: MutableState<String>,
    clipboardManager: ClipboardManager,
    onDone: () -> Unit = {}
) = withDesktopContext {
    val iconSend = rememberComputed {
        if (textState.value.isNotEmpty()) Send else null
    }
    SelectableOutlineEditText(
        modifier = modifier,
        value = textState.value,
        onValueChange = { t: String ->
            textState.value = t
            if (t.contains("\n")) {
                onDone()
            }
        },
        maxLines = 4,
        minLines = 4,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.White.copy(alpha = 0.1f),
            textColor = Color.White.copy(alpha = 0.9f),
            cursorColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Black
        ),
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (textState.value.isNotEmpty()) {
                    Icon(
                        modifier = Modifier.padding(4.dp)
                            .size(24.dp)
                            .onMousePress { // on click
                                clipboardManager.setText(AnnotatedString(textState.value))
                            },
                        imageVector = ContentCopy,
                        tint = Color.White.copy(alpha = 0.9f),
                        contentDescription = ""
                    )
                }
                if (iconSend.value != null) {
                    Icon(
                        modifier = Modifier.padding(4.dp)
                            .size(24.dp)
                            .onMousePress { // on click
                                onDone()
                            },
                        imageVector = iconSend.value!!,
                        tint = Color.White.copy(alpha = 0.9f),
                        contentDescription = ""
                    )
                }
            }
        }
    )
}