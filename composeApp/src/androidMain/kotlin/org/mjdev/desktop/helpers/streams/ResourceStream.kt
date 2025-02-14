package org.mjdev.desktop.helpers.streams

class ResourceStream(
    private val resName: String,
    val bytes: ByteArray = ByteArray(0) // to
//        useResource(resName) { stream ->
//        stream.readAllBytes()
//    }
) {
    val string get() = String(bytes)
}
