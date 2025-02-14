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
class ProviderErzvo(
    private val loadCount: Int = 10
) : ImagesProvider {
    override val size: Int = 1
    override suspend fun get(): Any {
        return "http://erzvo.com/wp-content/uploads/2022/11/22-11-05-10429-Kubo-kvety-2013-1.jpg"
    }
}