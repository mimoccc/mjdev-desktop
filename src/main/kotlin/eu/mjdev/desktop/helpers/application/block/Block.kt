/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

@file:Suppress("unused")

package eu.mjdev.desktop.helpers.application.block

typealias BLOCK = () -> Unit

typealias SCOPED_BLOCK<T> = T.() -> Unit

fun <B : BLOCK> List<B>.invokeAll() = forEach { block -> block.invoke() }

fun <S> MutableList<SCOPED_BLOCK<S>>.invokeAll(scope: S) = forEach { block -> block.invoke(scope) }

fun <S> MutableList<SCOPED_BLOCK<S>>.invokeAllOnce(scope: S) = iterator().apply {
    while (hasNext()) {
        val block = next()
        block.invoke(scope)
        remove()
    }
}
