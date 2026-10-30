package org.psippr.desktop.components.controlcenter.pages.devices

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import org.psippr.desktop.components.controlcenter.pages.plugins.PluginsPageDefaults
import org.psippr.desktop.components.controlcenter.settings.SettingsInfoRow
import org.psippr.desktop.components.text.TextAny
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.extensions.Colors.alpha
import org.psippr.desktop.managers.devices.ConnectedDevice
import org.psippr.desktop.managers.devices.IDevicesManager

/**
 * One device in the devices list: icon, name and a short summary. Clicking it expands the
 * device's info and details, then its actions; clicking again collapses it. Drawn like a plugin
 * row of the Plugins tab, so both lists look the same.
 */
@Suppress("FunctionName")
@Composable
fun DeviceRow(
    device: ConnectedDevice,
    manager: IDevicesManager,
    modifier: Modifier = Modifier,
) = withDesktopContext {
    var expanded by remember(device.id) { mutableStateOf(false) }
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
            Icon(
                imageVector = Icons.Filled.Smartphone,
                contentDescription = "",
                tint = textColor,
                modifier = Modifier.size(PluginsPageDefaults.iconSize),
            )
            Column(modifier = Modifier.weight(1f)) {
                TextAny(
                    text = device.name,
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                )
                if (device.summary.isNotBlank()) {
                    TextAny(
                        text = device.summary,
                        color = textColor.alpha(PluginsPageDefaults.SECONDARY_ALPHA),
                        fontSize = 12.sp,
                        singleLine = true,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = PluginsPageDefaults.rowPadding)) {
                device.info.forEach { (label, value) -> SettingsInfoRow(label = label, value = value) }
                DeviceActionRows(device = device, manager = manager)
            }
        }
    }
}
