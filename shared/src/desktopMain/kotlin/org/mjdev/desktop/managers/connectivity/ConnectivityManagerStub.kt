/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.connectivity

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.data.BthDevice
import org.mjdev.desktop.data.EthNetwork
import org.mjdev.desktop.data.NetDevice
import org.mjdev.desktop.data.WifiNetwork

open class ConnectivityManagerStub(
    val context: IDesktopContext,
) : IConnectivityManager {
    override val allDevices: MutableMap<String, NetDevice> = mutableMapOf()

    override val ethDevices: MutableMap<String, NetDevice> = mutableMapOf()

    override val wifiDevices: MutableMap<String, NetDevice> = mutableMapOf()

    override val bthDevices: MutableMap<String, NetDevice> = mutableMapOf()

    override val connectedDevices: MutableMap<String, NetDevice> = mutableMapOf()

    override val ethNetworks: MutableMap<String, EthNetwork> = mutableMapOf()

    override val wifiNetworks: MutableMap<String, WifiNetwork> = mutableMapOf()

    override val bthNetworks: MutableMap<String, BthDevice> = mutableMapOf()

    override val isWifiAdapterAvailable: Boolean
        get() = wifiDevices.isNotEmpty()

    override val isEthAdapterAvailable
        get() = ethDevices.isNotEmpty()

    override val isBthAdapterAvailable
        get() = bthDevices.isNotEmpty()

    override val hasConnectedDevices
        get() = connectedDevices.isNotEmpty()

    override fun connectWifi(ssid: String): Result<Boolean> = Result.success(false)

    override fun connectWifi(
        ssid: String,
        password: String,
        deviceName: String,
        store: Boolean,
    ): Result<Boolean> = Result.success(false)
}
