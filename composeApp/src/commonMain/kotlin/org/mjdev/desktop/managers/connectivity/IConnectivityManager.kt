package org.mjdev.desktop.managers.connectivity

import org.mjdev.desktop.data.BthDevice
import org.mjdev.desktop.data.EthNetwork
import org.mjdev.desktop.data.NetDevice
import org.mjdev.desktop.data.WifiNetwork
import org.mjdev.desktop.managers.base.IDelegate

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

    fun connectWifi(
        ssid: String
    ): Result<Boolean> = Result.success(false)

    fun connectWifi(
        ssid: String,
        password: String,
        deviceName: String = "",
        store: Boolean = true
    ): Result<Boolean> = Result.success(false)


    companion object {
        val EMPTY = object : IConnectivityManager {
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
