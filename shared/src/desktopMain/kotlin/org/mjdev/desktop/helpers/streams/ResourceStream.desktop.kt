package org.mjdev.desktop.helpers.streams

import java.io.InputStream

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "MemberVisibilityCanBePrivate")
actual class ResourceStream actual constructor(
    val resourcePath: String,
) {
    actual val bytes: ByteArray
        get() = useResource(resourcePath) { stream -> stream.readBytes() }
    actual val text
        get() = String(bytes)

    companion object {
        inline fun <T> useResource(
            resourcePath: String,
            block: (InputStream) -> T,
        ): T = openResource(resourcePath).use(block)

        fun openResource(resourcePath: String): InputStream = ClassLoaderResourceLoader.load(resourcePath)

        private object ClassLoaderResourceLoader {
            fun load(resourcePath: String): InputStream {
                val resource =
                    Thread
                        .currentThread()
                        .contextClassLoader
                        ?.getResourceAsStream(resourcePath)
                        ?: this::class.java.getResourceAsStream(resourcePath)
                return requireNotNull(resource) {
                    "Resource $resourcePath not found"
                }
            }
        }
    }
}
