package org.mjdev.desktop.components.button

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Compose.preview

@Composable
fun ButtonPrimary(
    text: String = "Button",
    enabled: Boolean = true,
    visible: Boolean = true,
    onClick: () -> Unit = {},
) = withDesktopContext {
    if (visible) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors =
                ButtonDefaults.buttonColors(
                    backgroundColor = iconsTintColor,
                    disabledBackgroundColor = disabledColor,
                ),
        ) {
            TextAny(
                text = text,
                color = textColor,
            )
        }
    }
}

@Preview
@Composable
fun PreviewButtonPrimary() =
    preview {
        ButtonPrimary()
    }
