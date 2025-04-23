package org.mjdev.desktop.helpers.streams

// todo: implement this for wasmJs
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ResourceStream actual constructor(
    val resourcePath: String
) {
    actual val bytes: ByteArray
        get() = byteArrayOf()
    actual val text: String
        get() = ""
}