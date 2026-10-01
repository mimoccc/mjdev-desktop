package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview

/** A read-only row showing a palette color as a swatch next to its [label] and hex value. */
@Suppress("FunctionName")
@Composable
fun SettingsColorRow(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) = withDesktopContext {
    val shape = RoundedCornerShape(SettingsDefaults.cornerRadius / 2)
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
            color = textColor,
            fontSize = 14.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextAny(
                text = color.toHex(),
                color = textColor.alpha(SettingsDefaults.SECONDARY_ALPHA),
                fontSize = 12.sp,
            )
            Box(
                modifier =
                    Modifier
                        .size(SettingsDefaults.swatchSize)
                        .background(color, shape)
                        .border(
                            SettingsDefaults.swatchBorderWidth,
                            textColor.alpha(SettingsDefaults.BORDER_ALPHA),
                            shape,
                        ),
            )
        }
    }
}

/** Formats an opaque RGB hex string, e.g. `#1A2B3C`, ignoring alpha. */
private fun Color.toHex(): String {
    val rgb =
        (red * COMPONENT_MAX).toInt().shl(RED_SHIFT) or
            (green * COMPONENT_MAX).toInt().shl(GREEN_SHIFT) or
            (blue * COMPONENT_MAX).toInt()
    return "#" + rgb.toString(HEX_RADIX).padStart(HEX_DIGITS, '0').uppercase()
}

private const val COMPONENT_MAX = 255
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val HEX_RADIX = 16
private const val HEX_DIGITS = 6

@Preview
@Composable
fun PreviewSettingsColorRow() = preview {
    SettingsColorRow(label = "Background", color = Color.DarkGray)
}
