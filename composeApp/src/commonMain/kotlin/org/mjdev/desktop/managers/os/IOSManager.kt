package org.mjdev.desktop.managers.os

import org.mjdev.desktop.managers.base.IDelegate

interface IOSManager : IDelegate {
    val machineName: String

    companion object {
        val EMPTY = object : IOSManager {
            override val machineName: String = "mjdev"
        }
    }
}
