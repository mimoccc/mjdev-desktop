package eu.mjdev.desktop.components.button

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Composable
fun ButtonPrimary(
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
                backgroundColor = iconsTintColor,
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
fun ButtonPrimaryPreview() = Box(
    modifier = Modifier.fillMaxSize().background(Color.SuperDarkGray)
) {
    ButtonPrimary()
}
