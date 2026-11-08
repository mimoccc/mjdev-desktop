package org.psippr.desktop.components.controlcenter.pages.ethernet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.psippr.desktop.components.controlcenter.settings.SettingsButtonRow
import org.psippr.desktop.components.controlcenter.settings.SettingsSection
import org.psippr.desktop.components.controlcenter.settings.SettingsSelectRow
import org.psippr.desktop.components.controlcenter.settings.SettingsSwitchRow
import org.psippr.desktop.components.controlcenter.settings.SettingsTextFieldRow
import org.psippr.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.psippr.desktop.data.Ipv4Settings
import org.psippr.desktop.data.Ipv4Validator
import org.psippr.desktop.managers.connectivity.IConnectivityManager

/**
 * IPv4 settings of the connection the wired adapter [device] uses: automatic (DHCP) or manual
 * address, gateway and DNS, and whether it connects by itself. Nothing changes until Apply; the
 * input is checked first and the result is shown as a notification. Hidden while the adapter has
 * no connection profile.
 */
@Suppress("FunctionName")
@Composable
fun EthIpv4Section(
    device: String,
    manager: IConnectivityManager,
) = withDesktopContext {
    val scope = rememberCoroutineScope()
    var settings by remember(device) { mutableStateOf<Ipv4Settings?>(null) }
    var applying by remember(device) { mutableStateOf(false) }
    LaunchedEffect(device) {
        settings = withContext(Dispatchers.IO) { manager.ipv4Settings(device) }
    }
    val current = settings ?: return@withDesktopContext
    SettingsSection(title = EthPageDefaults.IPV4_TITLE + device) {
        SettingsSelectRow(
            label = EthPageDefaults.METHOD,
            selected = if (current.automatic) EthPageDefaults.AUTOMATIC else EthPageDefaults.MANUAL,
            options = listOf(EthPageDefaults.AUTOMATIC, EthPageDefaults.MANUAL),
        ) { choice -> settings = current.copy(automatic = choice == EthPageDefaults.AUTOMATIC) }
        if (!current.automatic) {
            SettingsTextFieldRow(
                label = EthPageDefaults.ADDRESS,
                value = current.address,
                placeholder = EthPageDefaults.ADDRESS_HINT,
                onValueChange = { text -> settings = current.copy(address = text) },
            )
            SettingsTextFieldRow(
                label = EthPageDefaults.GATEWAY,
                value = current.gateway,
                placeholder = EthPageDefaults.GATEWAY_HINT,
                onValueChange = { text -> settings = current.copy(gateway = text) },
            )
            SettingsTextFieldRow(
                label = EthPageDefaults.DNS,
                value = current.dns,
                placeholder = EthPageDefaults.DNS_HINT,
                onValueChange = { text -> settings = current.copy(dns = text) },
            )
        }
        SettingsSwitchRow(
            label = EthPageDefaults.AUTOCONNECT,
            checked = current.autoconnect,
            enabled = !applying,
            onCheckedChange = { on -> settings = current.copy(autoconnect = on) },
        )
        SettingsButtonRow(
            label = EthPageDefaults.APPLY_LABEL,
            action = if (applying) EthPageDefaults.APPLYING else EthPageDefaults.APPLY,
            enabled = !applying,
        ) {
            val problem = Ipv4Validator.problem(current)
            if (problem != null) {
                context.notify(title = device, message = problem)
                return@SettingsButtonRow
            }
            applying = true
            scope.launch {
                val done = withContext(Dispatchers.IO) { manager.applyIpv4Settings(current) }.getOrDefault(false)
                context.notify(title = device, message = if (done) EthPageDefaults.APPLIED else EthPageDefaults.REFUSED)
                settings = withContext(Dispatchers.IO) { manager.ipv4Settings(device) } ?: current
                applying = false
            }
        }
    }
}
