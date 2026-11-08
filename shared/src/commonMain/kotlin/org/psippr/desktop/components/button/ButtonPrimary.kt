package org.psippr.desktop.components.button

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Compose.preview

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
fun PreviewButtonPrimary() = preview {
    ButtonPrimary()
}
