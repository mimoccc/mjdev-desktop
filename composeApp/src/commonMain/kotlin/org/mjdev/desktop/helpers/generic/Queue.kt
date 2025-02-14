/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.generic

import org.mjdev.desktop.log.Log

@Suppress("unused")
class Queue<E>(
    val source: List<E>
) : Iterator<E> {
    private var currentIndex = 0

    override fun hasNext(): Boolean =
        source.isNotEmpty() && source.size > currentIndex

    @Deprecated("Deprecated please use for safety nextOrNull().")
    override fun next(): E = when {
        hasNext() -> {
            source[currentIndex].apply {
                currentIndex += 1
            }
        }

        source.isEmpty() -> throw (IllegalStateException("Source is empty."))

        else -> {
            currentIndex = 0
            @Suppress("DEPRECATION")
            next()
        }
    }

    fun nextOrNull(): E? = runCatching {
        when {
            hasNext() -> {
                source[currentIndex].apply {
                    Log.d("New background request: $this")
                    currentIndex += 1
                }
            }

            source.isEmpty() -> {
                Log.e("No background, empty list.")
                null
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
