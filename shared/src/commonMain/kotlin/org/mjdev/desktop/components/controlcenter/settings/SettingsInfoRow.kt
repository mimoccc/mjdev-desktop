package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview

/** A read-only "label : value" row colored from the wallpaper palette. */
@Suppress("FunctionName")
@Composable
fun SettingsInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
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
            color = textColor,
            fontSize = 14.sp,
        )
        TextAny(
            modifier = Modifier.padding(start = 16.dp),
            text = value.ifBlank { SettingsDefaults.EMPTY_VALUE },
            color = textColor.alpha(SettingsDefaults.SECONDARY_ALPHA),
            fontSize = 14.sp,
            textAlign = TextAlign.End,
        )
    }
}

@Preview
@Composable
fun PreviewSettingsInfoRow() = preview {
    SettingsInfoRow(label = "Host", value = "mjdev-laptop")
}
