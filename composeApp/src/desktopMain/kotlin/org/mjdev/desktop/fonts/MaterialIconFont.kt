@file:Suppress("unused")

package org.mjdev.desktop.fonts

import androidx.compose.ui.text.font.FontFamily
import org.mjdev.desktop.interfaces.IFont
import org.mjdev.desktop.helpers.fuzzywuzzy.FuzzySearch

class MaterialIconFont(
    private val ttfFileName: String,
    private val codepointsFileName: String,
    private val fontFile: FontFile = FontFile(ttfFileName),
    private val codePointsFile: CodePointsFile = CodePointsFile(codepointsFileName)
) : IFont {
    override val fontFamily: FontFamily
        get() = fontFile.fontFamily

    private val cache: MutableMap<String, Int> = mutableMapOf()

    override fun iconForName(
        iconName: String?,
    ): Int? = runCatching {
        if (iconName != null) {
            if (cache.containsKey(iconName)) {
                cache[iconName]
            } else {
                codePointsFile.icons.map { icon ->
                    Pair(FuzzySearch.ratio(iconName, icon.key), icon)
                }.maxByOrNull { it.first }?.second?.value?.also {
                    cache[iconName] = it
                }
            }
        } else null
    }.getOrNull()
}

val MaterialSymbolsOutlined by lazy {
    MaterialIconFont(
        "icons/MaterialSymbolsOutlined.ttf",
        "icons/MaterialSymbolsOutlined.codepoints"
    )
}

val MaterialSymbolsRounded by lazy {
    MaterialIconFont(
        "icons/MaterialSymbolsRounded.ttf",
        "icons/MaterialSymbolsRounded.codepoints"
    )
}

val MaterialSymbolsSharp by lazy {
    MaterialIconFont(
        "icons/MaterialSymbolsSharp.ttf",
        "icons/MaterialSymbolsSharp.codepoints"
    )
}
