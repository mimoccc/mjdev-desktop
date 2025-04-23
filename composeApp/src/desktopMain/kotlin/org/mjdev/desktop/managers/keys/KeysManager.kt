package org.mjdev.desktop.managers.keys

import org.mjdev.desktop.helpers.streams.ResourceStream

class KeysManager : IKeyManager {
    override fun loadKey(key: String): String = runCatching {
        ResourceStream("keys/$key.key").text
    }.getOrNull().orEmpty()
}