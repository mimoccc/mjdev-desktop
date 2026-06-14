package org.mjdev.desktop.helpers.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.svg.SvgDecoder

object ImageLoader {
    @Composable
    fun imageLoaderContext() = LocalPlatformContext.current

    @Composable
    fun imageLoaderMemoryCache() =
        MemoryCache
            .Builder()
            .maxSizePercent(imageLoaderContext(), 0.3)
            .strongReferencesEnabled(true)
            .build()

    @Composable
    fun asyncImageLoader(
        imageLoaderContext: PlatformContext = imageLoaderContext(),
        imageLoaderMemoryCache: MemoryCache = imageLoaderMemoryCache(),
    ) = ImageLoader
        .Builder(imageLoaderContext)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .components {
            add(SvgDecoder.Factory())
//            add(GifDecoder.Factory())
        }.memoryCache { imageLoaderMemoryCache }
        .build()
}
