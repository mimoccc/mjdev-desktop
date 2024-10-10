package eu.mjdev.desktop.extensions

import java.util.Locale

@Suppress("DEPRECATION")
object Locale {
    fun String.toLocale(): Locale = runCatching {
        split("_").let { lc ->
            when (lc.size) {
                2 -> Locale(lc[0], lc[1])
                1 -> Locale(lc[0])
                else -> Locale.ENGLISH
            }
        }
    }.getOrNull() ?: Locale.ENGLISH
}
