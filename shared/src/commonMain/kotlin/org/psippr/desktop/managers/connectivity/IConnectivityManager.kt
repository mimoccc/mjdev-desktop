package org.psippr.desktop.managers.connectivity

import org.psippr.desktop.data.BthDevice
import org.psippr.desktop.data.EthNetwork
import org.psippr.desktop.data.Ipv4Settings
import org.psippr.desktop.data.NetDevice
import org.psippr.desktop.data.WifiNetwork
import org.psippr.desktop.managers.base.IDelegate

interface IConnectivityManager : IDelegate {
    val allDevices: MutableMap<String, NetDevice>

    val ethDevices: MutableMap<String, NetDevice>

    val wifiDevices: MutableMap<String, NetDevice>

    val bthDevices: MutableMap<String, NetDevice>

    val connectedDevices: MutableMap<String, NetDevice>

    val ethNetworks: MutableMap<String, EthNetwork>

    val wifiNetworks: MutableMap<String, WifiNetwork>

    val bthNetworks: MutableMap<String, BthDevice>

    val isWifiAdapterAvailable: Boolean

    val isEthAdapterAvailable: Boolean

    val isBthAdapterAvailable: Boolean

    val hasConnectedDevices: Boolean

    /** True while the bluetooth adapter is switched on. */
    val isBluetoothPowered: Boolean
        get() = false

    /** Connects or disconnects the network device named [device]; no-op where unsupported. */
    fun setDeviceConnected(
        device: String,
        connected: Boolean,
    ): Result<Boolean> = Result.success(false)

    /**
     * Details of the network device [device] (connection, addresses, DNS, ...) keyed by their
     * system field name, see [org.psippr.desktop.data.NetDetail]; empty when unsupported.
     */
    fun deviceDetails(device: String): Map<String, String> = emptyMap()

    /** IPv4 settings of the connection [device] uses, null when it has none or it is unsupported. */
    fun ipv4Settings(device: String): Ipv4Settings? = null

    /** Applies [settings] to their connection and brings it up again; false when refused. */
    fun applyIpv4Settings(settings: Ipv4Settings): Result<Boolean> = Result.success(false)

    /** Switches the bluetooth adapter on or off; no-op where unsupported. */
    fun setBluetoothPowered(powered: Boolean): Result<Boolean> = Result.success(false)

    /**
     * Searches for bluetooth devices around for a few seconds (blocking), so devices never seen
     * before show up in [bthNetworks]; no-op where unsupported.
     */
    fun scanBluetooth(): Result<Boolean> = Result.success(false)

    /** Connects or disconnects the bluetooth device with [address]; no-op where unsupported. */
    fun setBluetoothDeviceConnected(
        address: String,
        connected: Boolean,
    ): Result<Boolean> = Result.success(false)

    fun connectWifi(ssid: String): Result<Boolean> = Result.success(false)

    fun connectWifi(
        ssid: String,
        password: String,
        deviceName: String = "",
        store: Boolean = true,
    ): Result<Boolean> = Result.success(false)

    companion object {
        val EMPTY =
            object : IConnectivityManager {
                override val allDevices: MutableMap<String, NetDevice> = mutableMapOf()
                override val ethDevices: MutableMap<String, NetDevice> = mutableMapOf()
                override val wifiDevices: MutableMap<String, NetDevice> = mutableMapOf()
                override val bthDevices: MutableMap<String, NetDevice> = mutableMapOf()
                override val connectedDevices: MutableMap<String, NetDevice> = mutableMapOf()
                override val ethNetworks: MutableMap<String, EthNetwork> = mutableMapOf()
                override val wifiNetworks: MutableMap<String, WifiNetwork> = mutableMapOf()
                override val bthNetworks: MutableMap<String, BthDevice> = mutableMapOf()
                override val isWifiAdapterAvailable: Boolean = false
                override val isEthAdapterAvailable: Boolean = false
                override val isBthAdapterAvailable: Boolean = false
                override val hasConnectedDevices: Boolean = false
            }
    }
}
