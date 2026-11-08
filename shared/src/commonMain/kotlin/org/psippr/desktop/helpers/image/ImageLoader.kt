package org.psippr.desktop.helpers.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    // old helpers: built a NEW loader with a NEW empty cache on every recomposition, so AsyncImage
    // restarted and showed nothing until the picture was decoded again (user picture blinking)
//    @Composable
//    fun imageLoaderMemoryCache() = MemoryCache
//        .Builder()
//        .maxSizePercent(imageLoaderContext(), 0.3)
//        .strongReferencesEnabled(true)
//        .build()
//
//    @Composable
//    fun asyncImageLoader(
//        imageLoaderContext: PlatformContext = imageLoaderContext(),
//        imageLoaderMemoryCache: MemoryCache = imageLoaderMemoryCache(),
//    ) = ImageLoader
//        .Builder(imageLoaderContext)
//        .memoryCachePolicy(CachePolicy.ENABLED)
//        .components {
//            add(SvgDecoder.Factory())
// //            add(GifDecoder.Factory())
//        }.memoryCache { imageLoaderMemoryCache }
//        .build()

    /** Share of the available memory the image cache may use. */
    private const val MEMORY_CACHE_PERCENT = 0.3

    /** Memory cache kept across recompositions of the calling composable. */
    @Composable
    fun imageLoaderMemoryCache(): MemoryCache {
        val context = imageLoaderContext()
        return remember(context) {
            MemoryCache
                .Builder()
                .maxSizePercent(context, MEMORY_CACHE_PERCENT)
                .strongReferencesEnabled(true)
                .build()
        }
    }

    /** Image loader kept across recompositions, so loaded images stay on screen while colors animate. */
    @Composable
    fun asyncImageLoader(
        imageLoaderContext: PlatformContext = imageLoaderContext(),
        imageLoaderMemoryCache: MemoryCache = imageLoaderMemoryCache(),
    ): coil3.ImageLoader = remember(imageLoaderContext, imageLoaderMemoryCache) {
        ImageLoader
            .Builder(imageLoaderContext)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .components {
                add(SvgDecoder.Factory())
//            add(GifDecoder.Factory())
            }.memoryCache { imageLoaderMemoryCache }
            .build()
    }
}
