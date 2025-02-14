/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.providers.background

import org.mjdev.desktop.helpers.compose.ImagesProvider

// todo
@Suppress("unused")
class ProviderSmug(
    private val loadCount: Int = 10
) : ImagesProvider {
    private val list = listOf(
        "https://wallpapersmug.com/u/c78d6a/rainy-night-of-city-dark.jpeg",
        "https://wallpapersmug.com/u/db1982/vegeta-artwork.jpg",
        "https://wallpapersmug.com/u/6d3c26/dark-minimal-mountains.png",
        "https://wallpapersmug.com/u/4ef517/devil-boy-in-mask-dark.jpg",
        "https://wallpapersmug.com/u/c3c43f/matrix-code-numbers-green.jpg",
        "https://wallpapersmug.com/u/ee041b/computer-screen-code-program.jpg"
    )
    override val size: Int = list.size
    override suspend fun get(): Any = list
}