package org.mjdev.desktop.managers.keys

import org.mjdev.desktop.managers.base.IDelegate

interface IKeyManager : IDelegate {
    fun loadKey(key: String): String
}
