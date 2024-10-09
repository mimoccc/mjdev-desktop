/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.internal

interface ImagesProvider {
    suspend fun get(): Any?

    companion object {
        suspend fun ImagesProvider.getOne(): Any? = get().let { if (it is List<*>) it.firstOrNull() else it }
    }
}