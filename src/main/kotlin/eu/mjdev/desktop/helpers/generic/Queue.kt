/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.generic

import eu.mjdev.desktop.log.Log

@Suppress("unused")
class Queue<E>(
    val source: List<E>
) : Iterator<E> {
    var currentIndex = 0

    override fun hasNext(): Boolean =
        source.isNotEmpty() && source.size > currentIndex

    @Deprecated("Deprecated please use for safety nextOrNull().")
    override fun next(): E = when {
        source.isEmpty() -> throw (IllegalStateException("Source is empty."))
        hasNext() -> {
            source[currentIndex].apply {
                currentIndex += 1
            }
        }

        else -> {
            currentIndex = 0
            @Suppress("DEPRECATION")
            next()
        }
    }

    fun nextOrNull(): E? = runCatching {
        when {
            source.isEmpty() -> null
            hasNext() -> {
                source[currentIndex].apply {
                    currentIndex += 1
                }
            }

            else -> {
                currentIndex = 0
                @Suppress("DEPRECATION")
                next()
            }
        }
    }.onFailure { e ->
        Log.e(e)
    }.getOrNull()
}
