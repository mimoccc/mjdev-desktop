/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.data

sealed class LoadingState {
    object Initializing : LoadingState()
    data class Loading(val progress: Float) : LoadingState()
    object Finished : LoadingState()
}