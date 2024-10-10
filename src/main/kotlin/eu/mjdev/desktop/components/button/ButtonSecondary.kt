package eu.mjdev.desktop.components.button

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Composable
fun ButtonSecondary(
    text: String = "Button",
    enabled: Boolean = true,
    visible: Boolean = true,
    onClick: () -> Unit = {}
) = withDesktopScope {
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
