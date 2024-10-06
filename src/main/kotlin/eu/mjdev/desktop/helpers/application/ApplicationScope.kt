/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.application

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import eu.mjdev.desktop.extensions.Compose.launchedEffect
import eu.mjdev.desktop.helpers.application.block.SCOPED_BLOCK
import eu.mjdev.desktop.helpers.application.block.invokeAllOnce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@Suppress("MemberVisibilityCanBePrivate", "unused")
@Stable
open class ApplicationScope(
    val args: List<String> = emptyList()
) {
    private val isOpenState = mutableStateOf(true)
    private val sessionStartHandlers = mutableListOf<SCOPED_BLOCK<ApplicationScope>>()
    private val sessionEndHandlers = mutableListOf<SCOPED_BLOCK<ApplicationScope>>()

    var isOpen
        get() = isOpenState.value
        set(value) {
            isOpenState.value = value
        }

    fun dispatchStart() = CoroutineScope(Dispatchers.IO).launch {
        sessionStartHandlers.invokeAllOnce(this@ApplicationScope)
    }

    fun dispatchEnd() = CoroutineScope(Dispatchers.IO).launch {
        sessionEndHandlers.invokeAllOnce(this@ApplicationScope)
    }

    open fun exitApplication() {
        exitApplication(0)
    }

    fun exitApplication(code: Int) {
        exitProcess(code)
    }

    @Composable
    fun onSessionStart(
        block: ApplicationScope.() -> Unit
    ) {
        sessionStartHandlers.add(block)
        LaunchedEffect(sessionStartHandlers.size) {
            dispatchStart()
        }
    }

    @Composable
    fun onSessionEnd(
        block: ApplicationScope.() -> Unit
    ) {
        sessionEndHandlers.add(block)
    }

    @Composable
    fun render(
        onContentRendered: () -> Unit = {},
        content: @Composable ApplicationScope.() -> Unit
    ) = MaterialTheme {
        content()
        launchedEffect {
            onContentRendered()
        }
    }
}
