package org.mjdev.desktop.helpers.fuzzywuzzy.base

interface ToStringFunction<T> {
    fun apply(item: T?): String

    companion object {
        val NO_PROCESS: ToStringFunction<String> =
            object : ToStringFunction<String> {
                override fun apply(item: String?): String = item ?: ""
            }
    }
}
