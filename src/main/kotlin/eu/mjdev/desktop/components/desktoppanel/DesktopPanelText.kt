package eu.mjdev.desktop.components.desktoppanel

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.color
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Compose.onMouseEnter
import eu.mjdev.desktop.extensions.Compose.onMouseLeave
import eu.mjdev.desktop.extensions.Compose.rememberState
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Preview
@Composable
fun DesktopPanelText(
    modifier: Modifier = Modifier,
    text: String = "text",
    backgroundHover: Color = Color.White.copy(alpha = 0.4f),
    textPadding: PaddingValues = PaddingValues(4.dp),
    buttonState: MutableState<Boolean> = rememberState(false),
    onTooltip: (item: Any?) -> Unit = {},
    onClick: () -> Unit = {},
) = withDesktopScope {
    Box(
        modifier = modifier
            .wrapContentSize()
            .onMouseEnter {
                buttonState.value = true
                onTooltip(text)
            }
            .onMouseLeave {
                buttonState.value = false
            }
    ) {
        Button(
            modifier = Modifier.padding(textPadding),
            contentPadding = PaddingValues(0.dp),
            onClick = onClick,
            colors = ButtonDefaults.color(if (buttonState.value) backgroundHover else Color.Transparent),
            elevation = ButtonDefaults.noElevation()
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = text,
                color = textColor,
            )
        }
    }
}

@Preview
@Composable
fun DesktopPanelTextPreview() = DesktopPanelText()
