package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview

/** A labelled on/off setting backed by a Material switch. */
@Suppress("FunctionName")
@Composable
fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) = withDesktopContext {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextAny(
            text = label,
            color = if (enabled) textColor else textColor.alpha(0.4f),
            fontSize = 14.sp,
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = textColor,
                    checkedTrackColor = textColor.alpha(0.4f),
                    uncheckedThumbColor = textColor.alpha(0.6f),
                    uncheckedTrackColor = backgroundColor.alpha(0.6f),
                ),
        )
    }
}

@Preview
@Composable
fun PreviewSettingsSwitchRow() =
    preview {
        SettingsSwitchRow(
            label = "Local folder",
            checked = true,
            onCheckedChange = {},
        )
    }
