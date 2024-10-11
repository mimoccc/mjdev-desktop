/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.kcef.base

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import eu.mjdev.desktop.provider.DesktopProvider

open class KCEFManagerStub(
    val api: DesktopProvider
) {
    val initialized: MutableState<Boolean> = mutableStateOf(false)
    val restartRequired: MutableState<Boolean> = mutableStateOf(false)
    val downloading: MutableState<Int> = mutableStateOf(0)

    open fun init() {}

    open fun dispose() {}
}