package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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

/**
 * A labelled numeric setting backed by a slider. [valueRange] bounds the value and [steps]
 * controls discrete stops (0 = continuous). [format] renders the current value next to the label.
 */
@Suppress("FunctionName")
@Composable
fun SettingsSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    format: (Float) -> String = { it.toInt().toString() },
    onValueChange: (Float) -> Unit,
) = withDesktopContext {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextAny(
                text = label,
                color = if (enabled) textColor else textColor.alpha(0.4f),
                fontSize = 14.sp,
            )
            TextAny(
                text = format(value),
                color = textColor.alpha(0.7f),
                fontSize = 14.sp,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            colors =
                SliderDefaults.colors(
                    thumbColor = textColor,
                    activeTrackColor = textColor.alpha(0.7f),
                    inactiveTrackColor = backgroundColor.alpha(0.6f),
                ),
        )
    }
}

@Preview
@Composable
fun PreviewSettingsSliderRow() =
    preview {
        SettingsSliderRow(
            label = "Rotation delay",
            value = 60f,
            valueRange = 5f..600f,
            onValueChange = {},
        )
    }
