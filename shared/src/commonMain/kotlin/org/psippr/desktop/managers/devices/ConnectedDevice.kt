package org.psippr.desktop.managers.devices

/**
 * A device connected to the desktop, as one provider (e.g. ADB) sees it.
 *
 * @property id unique id, `<provider>:<provider's own id>`
 * @property provider name of the provider that handles the device's actions
 * @property name name shown as the section title
 * @property state whether it can be used now
 * @property summary one short line shown under the name in the list (e.g. system and battery)
 * @property info labelled facts (model, system, battery, connection, ...) in display order
 * @property actions what the user can do with it now
 */
data class ConnectedDevice(
    val id: String,
    val provider: String,
    val name: String,
    val state: DeviceState,
    val summary: String = "",
    val info: List<Pair<String, String>> = emptyList(),
    val actions: List<DeviceAction> = emptyList(),
)
