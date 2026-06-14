package org.mjdev.desktop.extensions

import androidx.compose.runtime.MutableState

object Text {
    fun <T> textFrom(text: T?): String =
        when (text) {
            is MutableState<*> -> textFrom(text.value)
            else -> text.toString()
        }.toString()

    fun String.notStartsWith(
        prefix: String,
        ignoreCase: Boolean = false,
    ) = !startsWith(prefix, ignoreCase)
}
