/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.keyevents

import androidx.compose.ui.input.key.Key

interface KeyEventListener

class KeyEventListenerCompose(
    val key: Key?,
    val block: (Char) -> Boolean
) : KeyEventListener

class KeyEventListenerAwt(
    val key: Int,
    val block: (Int) -> Boolean
) : KeyEventListener