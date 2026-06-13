/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.compose

interface ImagesProvider {
    val size: Int

    suspend fun get(): Any?

//    @Suppress("unused")
//    companion object {
//        suspend fun ImagesProvider.getOne(): Any? = get().let {
//            if (it is List<*>) it.firstOrNull() else it
//        }
//    }
}
