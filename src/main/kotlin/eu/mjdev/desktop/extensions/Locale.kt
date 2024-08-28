package eu.mjdev.desktop.extensions

import java.util.Locale

object Locale {

    fun String.toLocale(): Locale = this.split("_").let { lc ->
        when (lc.size) {
            2 -> Locale(lc[0], lc[1])
            1 -> Locale(lc[0])
            else -> java.util.Locale.ENGLISH
        }
    }

}