package org.mjdev.desktop.managers.keys

import kotlinx.coroutines.CoroutineScope
import org.mjdev.desktop.helpers.streams.ResourceStream
import org.mjdev.desktop.interfaces.IDesktopContext

class KeysManager(
    val context: IDesktopContext,
    val scope: CoroutineScope = context.scope
) : IKeyManager {
    override fun loadKey(key: String): String = runCatching {
        ResourceStream("keys/$key.key").text
    }.getOrNull().orEmpty()
}