package org.mjdev.desktop.helpers.streams

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ResourceStream(
    resourcePath: String
) {
    val bytes: ByteArray
    val text: String
}