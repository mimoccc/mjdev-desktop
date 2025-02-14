/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.data

import org.mjdev.desktop.helpers.parsers.Parsers.ParsedBoolean

data class WifiNetwork(
    val data: List<String> = emptyList(),
    val name: String = data.getOrNull(0).orEmpty(),
    val ssid: String = data.getOrNull(1).orEmpty(),
    val ssidHex: String = data.getOrNull(2).orEmpty(),
    private val a0: String = data.getOrNull(3).orEmpty(),
    private val a1: String = data.getOrNull(4).orEmpty(),
    private val a2: String = data.getOrNull(5).orEmpty(),
    private val a3: String = data.getOrNull(6).orEmpty(),
    private val a4: String = data.getOrNull(7).orEmpty(),
    private val a5: String = data.getOrNull(8).orEmpty(),
    val bssid: String = "$a0:$a1:$a2:$a3:$a4:$a5",
    val mode: String = data.getOrNull(9).orEmpty(),
    val channel: String = data.getOrNull(10).orEmpty(),
    val frequency: String = data.getOrNull(11).orEmpty(),
    val rate: String = data.getOrNull(12).orEmpty(),
    val bandwidth: String = data.getOrNull(13).orEmpty(),
    val signalLevel: String = data.getOrNull(14).orEmpty(),
    val signalGraph: String = data.getOrNull(15).orEmpty(),
    val encryption: String = data.getOrNull(16).orEmpty(),
    val wpaFlags: String = data.getOrNull(17).orEmpty(),
    val rsnFlags: String = data.getOrNull(18).orEmpty(),
    val device: String = data.getOrNull(19).orEmpty(),
    val isActive: Boolean = ParsedBoolean(data.getOrNull(20).orEmpty()),
    val isInUse: Boolean = ParsedBoolean(data.getOrNull(21).orEmpty()),
    val dbusPath: String = data.getOrNull(17).orEmpty(),
    val password: String = ""
) {
    override fun hashCode(): Int {
        return ssid.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
        other as WifiNetwork
//        if (isActive != other.isActive) return false
//        if (isInUse != other.isInUse) return false
//        if (data != other.data) return false
        if (name != other.name) return false
        if (ssid != other.ssid) return false
//        if (ssidHex != other.ssidHex) return false
//        if (a0 != other.a0) return false
//        if (a1 != other.a1) return false
//        if (a2 != other.a2) return false
//        if (a3 != other.a3) return false
//        if (a4 != other.a4) return false
//        if (a5 != other.a5) return false
        if (bssid != other.bssid) return false
        if (mode != other.mode) return false
        if (channel != other.channel) return false
        if (frequency != other.frequency) return false
        if (rate != other.rate) return false
        if (bandwidth != other.bandwidth) return false
//        if (signalLevel != other.signalLevel) return false
//        if (signalGraph != other.signalGraph) return false
        if (encryption != other.encryption) return false
        if (wpaFlags != other.wpaFlags) return false
        if (rsnFlags != other.rsnFlags) return false
        if (device != other.device) return false
        if (dbusPath != other.dbusPath) return false

        return true
    }
}