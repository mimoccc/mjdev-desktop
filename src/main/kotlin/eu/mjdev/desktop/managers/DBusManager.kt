/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers

import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder

class DBusManager {

    fun session () {
        DBusConnectionBuilder.forSessionBus()
            .receivingThreadConfig()
            .withSignalThreadCount(4)
//            .withMethodThreadCount(2)
            .connectionConfig()
            .withShared(true)
            .build().apply {
//                addSigHandler(this)
                connect()
            }
    }
}