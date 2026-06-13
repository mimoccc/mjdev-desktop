package org.mjdev.desktop.helpers.fuzzywuzzy.base

interface Applicable {
    fun apply(
        s1: String,
        s2: String,
    ): Int
}
