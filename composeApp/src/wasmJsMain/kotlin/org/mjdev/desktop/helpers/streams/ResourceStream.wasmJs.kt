package org.mjdev.desktop.helpers.streams

// todo: implement this for wasmJs
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ResourceStream actual constructor(
    val resourcePath: String
) {
    // todo
    actual val bytes: ByteArray
        get() = byteArrayOf()

    // todo
    actual val text: String
        get() = ""
}