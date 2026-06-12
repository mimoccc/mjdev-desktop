/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.mouseevents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.*
import java.awt.MouseInfo
import java.awt.Point

class GlobalMouseListener(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    val delay: Long = 200L,
    val onEvent: (event: Point) -> Unit
) {
    private var job: Job? = null
    private var lastPoint: Point? = null

    init {
        job = scope.launch {
            while (isActive) {
                MouseInfo.getPointerInfo().location.also { point ->
                    if (!point.equals(lastPoint)) {
                        lastPoint = point
                        onEvent(point)
                    }
                }
                delay(delay)
            }
        }
    }

    fun dispose() {
        job?.cancel()
    }

    companion object {
        @Composable
        fun globalMouseEventHandler(
            enabled: () -> Boolean = { true },
            scope: CoroutineScope = rememberCoroutineScope(),
            block: MouseEventHandler.() -> Unit,
        ) {
            DisposableEffect(Unit) {
                val handler = MouseEventHandler(enabled, block)
                val globalHandler = GlobalMouseListener(
                    scope = scope
                ) { event ->
                    handler.onEvent(event)
                }
                onDispose {
                    globalHandler.dispose()
                }
            }
        }
    }
}