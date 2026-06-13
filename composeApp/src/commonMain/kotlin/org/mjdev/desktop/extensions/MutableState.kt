package org.mjdev.desktop.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

object MutableStateExt {
    @Composable
    fun <T> rememberDerivedState(
        key: Any? = Unit,
        initialValue: T,
        block: suspend () -> T,
    ) = produceStateInCoroutine(initialValue, key, block)

    @Composable
    fun <T> produceStateInCoroutine(
        initialValue: T,
        key: Any? = null,
        block: suspend () -> T,
    ) = produceState(
        initialValue = initialValue,
        key1 = key,
    ) {
        withContext(Dispatchers.Default) {
            value = block()
        }
    }

    @Composable
    fun <T> rememberState(
        value: T,
        policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
    ) = remember { mutableStateOf(value, policy) }

    @Composable
    fun <T> rememberCalculated(
        vararg key: Any?,
        calculation: () -> T,
    ) = remember(*key) { derivedStateOf { calculation() } }

    @Composable
    fun <T> rememberComputed(
        vararg key: Any?,
        calculation: () -> T,
    ) = remember(*key) { derivedStateOf { calculation() } }

    fun MutableState<String>.clear() {
        value = ""
    }

    fun MutableState<String>.removeLast() {
        if (value.isNotEmpty()) value = value.take(value.length - 1)
    }

    operator fun MutableState<String>.plus(text: String) {
        value += text
    }

    operator fun MutableState<String>.plus(char: Char) {
        value += char
    }

    fun MutableState<Boolean>.toggle() {
        value = !value
    }

    fun <T> mutableStateListFlow(function: (List<T>) -> List<T>) =
        MutableStateFlow<List<T>>(emptyList()).apply {
            update(function)
        }
}
