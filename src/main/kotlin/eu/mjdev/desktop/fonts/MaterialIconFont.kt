@file:Suppress("unused")

package eu.mjdev.desktop.fonts

import androidx.compose.ui.text.font.FontFamily
import me.xdrop.fuzzywuzzy.FuzzySearch

class MaterialIconFont(
    private val ttfFileName: String,
    private val codepointsFileName: String,
    private val fontFile: FontFile = FontFile(ttfFileName),
    private val codePointsFile: CodePointsFile = CodePointsFile(codepointsFileName)
) {
    val fontFamily: FontFamily
        get() = fontFile.fontFamily

    private val cache: MutableMap<String, Int> = mutableMapOf()

    fun iconForName(
        name: String?,
    ): Int? = runCatching {
        if (name != null) {
            if (cache.containsKey(name)) {
                cache[name]
            } else {
                codePointsFile.icons.map { icon ->
                    Pair(FuzzySearch.ratio(name, icon.key), icon)
                }.maxByOrNull { it.first }?.second?.value?.also {
                    cache[name] = it
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
