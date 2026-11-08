package org.psippr.desktop.data

import org.psippr.desktop.helpers.compose.ImagesProvider

/** Test provider returning fixed [images], or throwing when [failure] is set. */
class TestImagesProvider(
    private val images: List<Any> = emptyList(),
    private val failure: Throwable? = null,
) : ImagesProvider {
    override val size: Int
        get() = images.size

    override suspend fun get(): Any = failure?.let { e -> throw e } ?: images
}
