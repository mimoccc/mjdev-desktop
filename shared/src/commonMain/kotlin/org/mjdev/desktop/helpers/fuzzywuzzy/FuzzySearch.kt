package org.mjdev.desktop.helpers.fuzzywuzzy

import org.mjdev.desktop.helpers.fuzzywuzzy.ratios.SimpleRatio

object FuzzySearch {
    fun ratio(
        s1: String,
        s2: String,
    ): Int = SimpleRatio().apply(s1, s2)
}
