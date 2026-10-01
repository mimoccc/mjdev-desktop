package org.mjdev.desktop.components.controlcenter.pages.plugins

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.controlcenter.settings.SettingsInfoRow
import org.mjdev.desktop.components.image.ImageAny
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.plugins.PluginInfo

/**
 * One plugin in the Plugins tab: image, label and an on/off switch. Clicking anywhere except the
 * switch expands or collapses the details (description, version, author, id, source, last error).
 */
@Suppress("FunctionName")
@Composable
fun PluginRow(
    info: PluginInfo,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) = withDesktopContext {
    var expanded by remember(info.id) { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(PluginsPageDefaults.rowShape)
                    .clickable { expanded = !expanded }
                    .padding(PluginsPageDefaults.rowPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PluginsPageDefaults.rowSpacing),
        ) {
            ImageAny(
                modifier = Modifier.size(PluginsPageDefaults.iconSize).clip(PluginsPageDefaults.rowShape),
                src = info.icon ?: PluginsPageDefaults.fallbackIcon,
                colorFilter = if (info.icon == null) ColorFilter.tint(textColor) else null,
                contentDescription = "",
            )
            Column(modifier = Modifier.weight(1f)) {
                TextAny(
                    text = info.metadata.name.ifBlank { info.id },
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!expanded && info.metadata.description.isNotBlank()) {
                    TextAny(
                        text = info.metadata.description,
                        color = textColor.alpha(PluginsPageDefaults.SECONDARY_ALPHA),
                        fontSize = 12.sp,
                        singleLine = true,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Switch(
                checked = info.enabled,
                onCheckedChange = onEnabledChange,
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = textColor,
                        checkedTrackColor = textColor.alpha(PluginsPageDefaults.TRACK_ALPHA),
                        uncheckedThumbColor = textColor.alpha(PluginsPageDefaults.SECONDARY_ALPHA),
                        uncheckedTrackColor = backgroundColor.alpha(PluginsPageDefaults.SECONDARY_ALPHA),
                    ),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = PluginsPageDefaults.rowPadding)) {
                TextAny(
                    modifier = Modifier.padding(bottom = PluginsPageDefaults.rowSpacing),
                    text = info.metadata.description.ifBlank { PluginsPageDefaults.NO_DESCRIPTION },
                    color = textColor.alpha(PluginsPageDefaults.SECONDARY_ALPHA),
                    fontSize = 13.sp,
                )
                SettingsInfoRow(PluginsPageDefaults.LABEL_VERSION, info.metadata.version)
                SettingsInfoRow(PluginsPageDefaults.LABEL_AUTHOR, info.metadata.author)
                SettingsInfoRow(PluginsPageDefaults.LABEL_ID, info.id)
                SettingsInfoRow(PluginsPageDefaults.LABEL_SOURCE, info.source)
                info.error?.let { SettingsInfoRow(PluginsPageDefaults.LABEL_ERROR, it) }
            }
        }
    }
}
