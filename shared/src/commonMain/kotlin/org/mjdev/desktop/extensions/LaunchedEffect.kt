package org.mjdev.desktop.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.mjdev.desktop.extensions.MutableStateExt.rememberState
import org.mjdev.desktop.extensions.System.currentTimeMillis
import kotlin.coroutines.CoroutineContext

@Suppress("ComposableNaming")
object LaunchedEffect {
    fun launch(
        context: CoroutineContext = Dispatchers.Default,
        scope: CoroutineScope = CoroutineScope(context),
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit,
    ) = scope.launch(context, start, block)

    @Composable
    fun LaunchedEffect(block: suspend CoroutineScope.() -> Unit) = LaunchedEffect(Unit, block)

    fun runAsync(
        context: CoroutineContext = Dispatchers.Main,
        block: suspend () -> Unit,
    ) = CoroutineScope(context).launch(context) { block() }

    // todo move to uiState
    @Composable
    fun <T> flowBlock(
        initial: T,
        delay: Long = 0L,
        coroutineContext: CoroutineContext = Dispatchers.Default,
        onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
        block: suspend () -> T,
    ): MutableState<T> {
        val result = remember { mutableStateOf(initial) }
        var state by rememberState(0L)
        LaunchedEffect(state) {
            flow {
                emit(block())
            }.catch { t ->
                onError(t)
            }.flowOn(coroutineContext)
                .collect { r ->
                    result.value = r
                }
            if (delay > 0L) {
                delay(delay)
                state = currentTimeMillis
            }
        }
        return result
    }

    // todo move to uiState
    @Composable
    fun <T> flowBlock(
        initial: T,
        key: Any,
        coroutineContext: CoroutineContext = Dispatchers.Default,
        onError: (Throwable) -> Unit = { e -> e.printStackTrace() },
        block: suspend () -> T,
    ): MutableState<T> {
        val result = remember { mutableStateOf(initial) }
        LaunchedEffect(key) {
            flow {
                emit(block())
            }.catch { t ->
                onError(t)
            }.flowOn(coroutineContext)
                .collect { r ->
                    result.value = r
                }
        }
        return result
    }
}
