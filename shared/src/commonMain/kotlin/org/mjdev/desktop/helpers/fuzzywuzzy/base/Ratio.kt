package org.mjdev.desktop.helpers.fuzzywuzzy.base

interface Ratio : Applicable {
    fun apply(
        s1: String,
        s2: String,
        sp: ToStringFunction<String>,
    ): Int
}
