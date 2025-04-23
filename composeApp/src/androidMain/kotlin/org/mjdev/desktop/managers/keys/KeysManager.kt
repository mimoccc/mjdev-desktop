package org.mjdev.desktop.managers.keys

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.mjdev.desktop.resources.Res
import java.io.File

class KeysManager {

    @OptIn(ExperimentalResourceApi::class)
    fun loadKey(key: String): String = runCatching {
        File(Res.getUri("keys/$key.key")).readText()
    }.getOrNull().orEmpty()

}
