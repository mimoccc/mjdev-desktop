package org.psippr.desktop.components.controlcenter.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.extensions.Compose.preview

/** Alpha of the border, as on [SettingsSelectRow]. */
private const val BORDER_ALPHA = 0.5f

/** Alpha of the placeholder and of a disabled field's text. */
private const val SECONDARY_ALPHA = 0.5f

/**
 * A labelled one line text input: [label] above, the field below in the full width (the
 * control center is narrow), drawn like the value box of [SettingsSelectRow]. [placeholder]
 * shows while the field is empty.
 */
@Suppress("FunctionName")
@Composable
fun SettingsTextFieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
) = withDesktopContext {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        TextAny(text = label, color = textColor, fontSize = 14.sp)
        Box(
            modifier =
                Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .border(1.dp, borderColor.alpha(BORDER_ALPHA), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                TextAny(text = placeholder, color = textColor.alpha(SECONDARY_ALPHA), fontSize = 14.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                cursorBrush = SolidColor(textColor),
                textStyle =
                    TextStyle(
                        color = if (enabled) textColor else textColor.alpha(SECONDARY_ALPHA),
                        fontSize = 14.sp,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
fun SettingsTextFieldRowPreview() = preview {
    SettingsTextFieldRow(label = "Address", value = "", onValueChange = {}, placeholder = "192.168.1.20/24")
}
