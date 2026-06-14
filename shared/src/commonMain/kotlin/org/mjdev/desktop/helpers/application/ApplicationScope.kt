/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.application

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import org.mjdev.desktop.extensions.LaunchedEffect.runAsync

@Suppress("MemberVisibilityCanBePrivate", "unused")
@Stable
open class ApplicationScope(
    val args: List<String> = emptyList(),
    var onExitProcess: ApplicationScope.(code: Int) -> Unit,
) {
    private val isOpenState = mutableStateOf(true)

    var isOpen
        get() = isOpenState.value
        set(value) {
            isOpenState.value = value
        }

    fun exitApplication() {
        exitApplication(0)
    }

    fun exitApplication(code: Int) =
        runAsync {
            onExitProcess(code)
            isOpen = false
        }
}
