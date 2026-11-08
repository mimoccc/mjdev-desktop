package org.psippr.desktop.components.controlcenter.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.extensions.Compose.preview

/**
 * A labelled single-choice list with every option visible, like the device lists of the Ubuntu
 * Settings Sound panel. Unlike a dropdown it needs no popup window, so it works in every
 * control center window.
 */
@Suppress("FunctionName")
@Composable
fun SettingsChoiceList(
    label: String,
    options: List<SettingsChoice>,
    modifier: Modifier = Modifier,
    onSelected: (SettingsChoice) -> Unit,
) = withDesktopContext {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = SettingsChoiceListDefaults.listPadding),
        verticalArrangement = Arrangement.spacedBy(SettingsChoiceListDefaults.rowSpacing),
    ) {
        TextAny(
            text = label,
            color = textColor,
            fontSize = 14.sp,
        )
        options.forEach { option ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(SettingsChoiceListDefaults.rowCorner))
                        .background(
                            if (option.selected) {
                                textColor.alpha(SettingsChoiceListDefaults.SELECTED_ALPHA)
                            } else {
                                Color.Transparent
                            },
                        ).clickable { if (!option.selected) onSelected(option) }
                        .padding(horizontal = SettingsChoiceListDefaults.rowPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = option.selected,
                    onClick = { if (!option.selected) onSelected(option) },
                    colors =
                        RadioButtonDefaults.colors(
                            selectedColor = textColor,
                            unselectedColor = textColor.alpha(SettingsDefaults.SECONDARY_ALPHA),
                        ),
                )
                TextAny(
                    text = option.label,
                    color = textColor,
                    fontSize = 14.sp,
                    maxLines = SettingsChoiceListDefaults.LABEL_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSettingsChoiceList() = preview {
    SettingsChoiceList(
        label = "Output device",
        options =
            listOf(
                SettingsChoice("a", "Speakers – Built-in Audio", true),
                SettingsChoice("b", "HDMI1 – Built-in Audio", false),
            ),
        onSelected = {},
    )
}
