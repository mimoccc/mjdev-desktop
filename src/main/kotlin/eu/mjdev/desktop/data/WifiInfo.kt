/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.data

data class WifiInfo(
    val data: List<String> = emptyList(),
    val id: String = data.getOrNull(0).orEmpty(),
    val ssid: String = data.getOrNull(1).orEmpty(),
    val password: String = data.getOrNull(2).orEmpty(),
    val a0: String = data.getOrNull(3).orEmpty(),
    val a1: String = data.getOrNull(4).orEmpty(),
    val a2: String = data.getOrNull(5).orEmpty(),
    val a3: String = data.getOrNull(6).orEmpty(),
    val a4: String = data.getOrNull(7).orEmpty(),
    val a5: String = data.getOrNull(8).orEmpty(),
    val infra: String = data.getOrNull(9).orEmpty(),
    val channel: String = data.getOrNull(10).orEmpty(),
    val frequency: String = data.getOrNull(11).orEmpty(),
    val speed: String = data.getOrNull(12).orEmpty(),
    val bandwidth: String = data.getOrNull(13).orEmpty(),
    val signalLevel: String = data.getOrNull(14).orEmpty(),
    val signalGraph: String = data.getOrNull(15).orEmpty(),
    val encryption: String = data.getOrNull(16).orEmpty(),
    val reserved1: String = data.getOrNull(17).orEmpty(),
    val encryptionDetails: String = data.getOrNull(18).orEmpty(),
    val adapter: String = data.getOrNull(19).orEmpty(),
    val connected: String = data.getOrNull(20).orEmpty(),
    val reserved2: String = data.getOrNull(21).orEmpty(),
    val mntp: String = data.getOrNull(17).orEmpty(),
)