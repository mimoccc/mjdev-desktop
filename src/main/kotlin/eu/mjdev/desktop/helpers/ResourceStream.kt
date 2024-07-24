package eu.mjdev.desktop.helpers

import androidx.compose.ui.res.useResource

class ResourceStream(
    private val resName: String,
    val bytes: ByteArray = useResource(resName) { stream ->
        stream.readAllBytes()
    }
) {
    val string get() = String(bytes)
}