@file:Suppress("unused")

package eu.mjdev.desktop.components.fonticon

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

    fun iconForName(
        name: String?,
    ): Int? = runCatching {
        if (name != null) {
            codePointsFile.icons.map { icon ->
                Pair(FuzzySearch.ratio(name, icon.key), icon)
            }.maxByOrNull { it.first }?.second?.value
        } else null
    }.getOrNull()
}

val MaterialSymbolsOutlined
    get() = MaterialIconFont(
        "icons/MaterialSymbolsOutlined.ttf",
        "icons/MaterialSymbolsOutlined.codepoints"
    )

val MaterialSymbolsRounded
    get() = MaterialIconFont(
        "icons/MaterialSymbolsRounded.ttf",
        "icons/MaterialSymbolsRounded.codepoints"
    )

val MaterialSymbolsSharp
    get() = MaterialIconFont(
        "icons/MaterialSymbolsSharp.ttf",
        "icons/MaterialSymbolsSharp.codepoints"
    )