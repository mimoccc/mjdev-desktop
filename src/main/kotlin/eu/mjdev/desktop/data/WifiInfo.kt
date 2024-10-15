/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.data

import eu.mjdev.desktop.extensions.Custom.ParsedBoolean

data class WifiInfo(
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
)