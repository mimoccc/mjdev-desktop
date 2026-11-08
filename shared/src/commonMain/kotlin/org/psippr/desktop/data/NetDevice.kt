/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.data

import okio.Path
import org.psippr.desktop.extensions.PathExt.exists
import org.psippr.desktop.extensions.PathExt.get
import org.psippr.desktop.extensions.PathExt.nameWithoutExtension
import org.psippr.desktop.extensions.PathExt.text

class NetDevice(
    val path: Path,
) {
    val name
        get() = path.nameWithoutExtension

    // todo
    val isEth
        get() = path["ethernet"].exists
    val isWifi
        get() = path["wireless"].exists
    val isBluetooth
        get() = path["device"]["bluetooth"].exists
    val isUp
        get() = path["operstate"].text.lowercase() == "up"
    val macAddress
        get() = path["address"].text

    override fun toString(): String =
        "NetDevice[$name](isEth=$isEth, isWifi=$isWifi, isBluetooth=$isBluetooth, isUp=$isUp, mac=$macAddress)"
}
