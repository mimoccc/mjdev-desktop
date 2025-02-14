package org.mjdev.desktop.helpers.streams

import androidx.compose.ui.res.useResource

@Suppress("DEPRECATION")
class ResourceStream(
    private val resName: String,
    val bytes: ByteArray = useResource(resName) { stream ->
        stream.readAllBytes()
    }
) {
    val string get() = String(bytes)
}