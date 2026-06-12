package org.mjdev.desktop.components.button

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.jetbrains.compose.ui.tooling.preview.Preview

// todo
@Composable
fun ButtonSecondary(
    text: String = "Button",
    enabled: Boolean = true,
    visible: Boolean = true,
    onClick: () -> Unit = {}
) = withDesktopContext {
    if (visible) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                disabledBackgroundColor = disabledColor
            )
        ) {
            TextAny(
                text = text,
                color = textColor
            )
        }
    }
}

@Preview
@Composable
fun PreviewButtonSecondary() = preview {
    ButtonSecondary()
}
