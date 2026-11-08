package org.psippr.desktop.managers.keys

import org.psippr.desktop.managers.base.IDelegate

interface IKeyManager : IDelegate {
    fun loadKey(key: String): String
}
