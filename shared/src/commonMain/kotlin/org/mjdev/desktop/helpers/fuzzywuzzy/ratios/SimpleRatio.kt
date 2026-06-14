package org.mjdev.desktop.helpers.fuzzywuzzy.ratios

import org.mjdev.desktop.helpers.fuzzywuzzy.base.Ratio
import org.mjdev.desktop.helpers.fuzzywuzzy.base.ToStringFunction
import org.mjdev.desktop.helpers.fuzzywuzzy.diffutils.DiffUtils
import kotlin.math.round

class SimpleRatio : Ratio {
    override fun apply(
        s1: String,
        s2: String,
    ): Int = round(100 * DiffUtils.getRatio(s1, s2)).toInt()

    override fun apply(
        s1: String,
        s2: String,
        sp: ToStringFunction<String>,
    ): Int = apply(sp.apply(s1), sp.apply(s2))
}
