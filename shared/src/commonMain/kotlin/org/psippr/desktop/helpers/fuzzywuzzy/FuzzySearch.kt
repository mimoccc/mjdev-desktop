package org.psippr.desktop.helpers.fuzzywuzzy

import org.psippr.desktop.helpers.fuzzywuzzy.ratios.SimpleRatio

object FuzzySearch {
    fun ratio(
        s1: String,
        s2: String,
    ): Int = SimpleRatio().apply(s1, s2)
}
