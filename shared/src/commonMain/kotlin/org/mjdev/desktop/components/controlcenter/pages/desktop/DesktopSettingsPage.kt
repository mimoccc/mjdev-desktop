package org.mjdev.desktop.components.controlcenter.pages.desktop

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.settings.SettingsSection
import org.mjdev.desktop.components.controlcenter.settings.SettingsSelectRow
import org.mjdev.desktop.components.controlcenter.settings.SettingsSliderRow
import org.mjdev.desktop.components.controlcenter.settings.SettingsSwitchRow
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.data.ControlCenterLocation
import org.mjdev.desktop.data.DesktopConfigStore
import org.mjdev.desktop.data.PanelLocation
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.settings.SettingsMonitor

/**
 * Control center page that configures the whole desktop. It loads the persisted
 * [org.mjdev.desktop.data.DesktopConfigData] from `~/.mjdev/desktop/config.json`, exposes every
 * tunable (background, panel, control center, app menu) and the background providers, and on
 * save writes the JSON back, applies it to the live theme and rebuilds the background sources.
 */
@Suppress("FunctionName")
fun DesktopSettingsPage(context: IDesktopContext) =
    ControlCenterPage(
        context = context,
        icon = SettingsMonitor,
        name = "Desktop",
    ) {
        val store = remember { DesktopConfigStore(currentUser) }
        var data by remember { mutableStateOf(store.load()) }

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 56.dp),
            ) {
                SettingsSection(title = "Background") {
                    SettingsSliderRow(
                        label = "Rotation delay",
                        value = data.backgroundRotationDelay.toFloat(),
                        valueRange = 5_000f..600_000f,
                        format = { "${(it / 1000f).toInt()}s" },
                        onValueChange = { data = data.copy(backgroundRotationDelay = it.toLong()) },
                    )
                    data.providers.forEach { provider ->
                        SettingsSwitchRow(
                            label = provider.label,
                            checked = provider.enabled,
                            onCheckedChange = { enabled ->
                                data =
                                    data.copy(
                                        providers =
                                            data.providers
                                                .map { p -> if (p.id == provider.id) p.copy(enabled = enabled) else p }
                                                .toMutableList(),
                                    )
                            },
                        )
                        if (provider.enabled && provider.hasLoadCount) {
                            SettingsSliderRow(
                                label = "  ${provider.label} images",
                                value = provider.loadCount.toFloat(),
                                valueRange = 1f..50f,
                                steps = 48,
                                onValueChange = { count ->
                                    data =
                                        data.copy(
                                            providers =
                                                data.providers
                                                    .map { p -> if (p.id == provider.id) p.copy(loadCount = count.toInt()) else p }
                                                    .toMutableList(),
                                        )
                                },
                            )
                        }
                    }
                }

                SettingsSection(title = "Panel") {
                    SettingsSelectRow(
                        label = "Location",
                        selected = data.panelLocation,
                        options = PanelLocation.entries.map { it.name },
                        onSelected = { data = data.copy(panelLocation = it) },
                    )
                    SettingsSliderRow(
                        label = "Auto-hide delay",
                        value = data.panelHideDelay.toFloat(),
                        valueRange = 0f..10_000f,
                        format = { if (it <= 0f) "off" else "${(it / 1000f).toInt()}s" },
                        onValueChange = { data = data.copy(panelHideDelay = it.toLong()) },
                    )
                    SettingsSliderRow(
                        label = "Divider width",
                        value = data.panelDividerWidth,
                        valueRange = 0f..32f,
                        format = { "${it.toInt()}dp" },
                        onValueChange = { data = data.copy(panelDividerWidth = it) },
                    )
                    SettingsSliderRow(
                        label = "Content padding",
                        value = data.panelContentPadding,
                        valueRange = 0f..32f,
                        format = { "${it.toInt()}dp" },
                        onValueChange = { data = data.copy(panelContentPadding = it) },
                    )
                }

                SettingsSection(title = "Control center") {
                    SettingsSelectRow(
                        label = "Location",
                        selected = data.controlCenterLocation,
                        options = ControlCenterLocation.entries.map { it.name },
                        onSelected = { data = data.copy(controlCenterLocation = it) },
                    )
                    SettingsSliderRow(
                        label = "Auto-hide delay",
                        value = data.controlPanelHideDelay.toFloat(),
                        valueRange = 0f..10_000f,
                        format = { if (it <= 0f) "off" else "${(it / 1000f).toInt()}s" },
                        onValueChange = { data = data.copy(controlPanelHideDelay = it.toLong()) },
                    )
                    SettingsSliderRow(
                        label = "Expanded width",
                        value = data.controlCenterExpandedWidthPercent.toFloat(),
                        valueRange = 10f..80f,
                        steps = 69,
                        format = { "${it.toInt()}%" },
                        onValueChange = { data = data.copy(controlCenterExpandedWidthPercent = it.toInt()) },
                    )
                    SettingsSliderRow(
                        label = "Divider width",
                        value = data.controlCenterDividerWidth,
                        valueRange = 0f..32f,
                        format = { "${it.toInt()}dp" },
                        onValueChange = { data = data.copy(controlCenterDividerWidth = it) },
                    )
                    SettingsSliderRow(
                        label = "Icon size",
                        value = data.controlCenterIconSize,
                        valueRange = 16f..64f,
                        format = { "${it.toInt()}dp" },
                        onValueChange = { data = data.copy(controlCenterIconSize = it) },
                    )
                    SettingsSliderRow(
                        label = "Background opacity",
                        value = data.controlCenterBackgroundAlpha,
                        valueRange = 0f..1f,
                        format = { "${(it * 100).toInt()}%" },
                        onValueChange = { data = data.copy(controlCenterBackgroundAlpha = it) },
                    )
                }

                SettingsSection(title = "App menu") {
                    SettingsSliderRow(
                        label = "Min width",
                        value = data.appMenuMinWidthRatio,
                        valueRange = 0.1f..1f,
                        format = { "${(it * 100).toInt()}%" },
                        onValueChange = { data = data.copy(appMenuMinWidthRatio = it) },
                    )
                    SettingsSliderRow(
                        label = "Min height",
                        value = data.appMenuMinHeightRatio,
                        valueRange = 0.1f..1f,
                        format = { "${(it * 100).toInt()}%" },
                        onValueChange = { data = data.copy(appMenuMinHeightRatio = it) },
                    )
                    SettingsSliderRow(
                        label = "Outer padding",
                        value = data.appMenuOuterPadding,
                        valueRange = 0f..32f,
                        format = { "${it.toInt()}dp" },
                        onValueChange = { data = data.copy(appMenuOuterPadding = it) },
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, borderColor.alpha(0.6f), RoundedCornerShape(8.dp))
                        .clickable {
                            store.save(data)
                            data.applyTo(currentUser.theme)
                            runAsync {
                                currentUser.config.reloadBackgrounds(data.buildProviders(currentUser))
                            }
                        }.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextAny(
                    text = "Save & apply",
                    color = textColor,
                    fontSize = 16.sp,
                )
            }
        }
    }

@Preview
@Composable
fun PreviewDesktopSettingsPage() =
    preview {
        DesktopSettingsPage(context).Render()
    }
