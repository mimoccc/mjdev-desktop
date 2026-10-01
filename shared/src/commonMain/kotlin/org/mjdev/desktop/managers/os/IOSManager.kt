package org.mjdev.desktop.managers.os

import org.mjdev.desktop.managers.base.IDelegate

interface IOSManager : IDelegate {
    val machineName: String

    /** Human readable distribution name, e.g. `Ubuntu 24.04 LTS`; empty when unknown. */
    val prettyName: String
        get() = ""

    /** Distribution home page; empty when unknown. */
    val homeUrl: String
        get() = ""

    /** Where to get support for the distribution; empty when unknown. */
    val supportUrl: String
        get() = ""

    /** Kernel release, e.g. `6.8.0-45-generic`; empty when unknown. */
    val kernel: String
        get() = ""

    /** How long the machine has been running, human readable; empty when unknown. */
    val uptime: String
        get() = ""

    companion object {
        val EMPTY =
            object : IOSManager {
                override val machineName: String = "mjdev"
            }
    }
}
