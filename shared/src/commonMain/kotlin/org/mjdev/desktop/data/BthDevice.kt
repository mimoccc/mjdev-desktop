/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.data

data class BthDevice(
    val name: String = "",
    /** Bluetooth hardware address, e.g. `AA:BB:CC:DD:EE:FF`; empty when unknown. */
    val address: String = "",
    /** True while the device is connected. */
    val connected: Boolean = false,
    /** True when the device has been paired before. */
    val paired: Boolean = false,
)
