package org.mjdev.desktop.components.controlcenter.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview

/** A labelled single-choice setting that opens a dropdown of [options]. */
@Suppress("FunctionName")
@Composable
fun SettingsSelectRow(
    label: String,
    selected: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit,
) = withDesktopContext {
    var expanded by remember { mutableStateOf(false) }
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
        Box {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .border(1.dp, borderColor.alpha(0.5f), RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                TextAny(
                    text = selected,
                    color = textColor,
                    fontSize = 14.sp,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(8.dp),
                containerColor = backgroundColor,
                border = BorderStroke(1.dp, borderColor.alpha(0.5f)),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            TextAny(
                                text = option,
                                color = textColor,
                                fontSize = 14.sp,
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelected(option)
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSettingsSelectRow() =
    preview {
        SettingsSelectRow(
            label = "Panel location",
            selected = "Bottom",
            options = listOf("Bottom", "Top", "Left", "Right"),
            onSelected = {},
        )
    }
