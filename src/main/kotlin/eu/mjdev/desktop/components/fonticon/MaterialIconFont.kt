@file:Suppress("unused")

package eu.mjdev.desktop.components.fonticon

class MaterialIconFont(
    private val ttfFileName: String,
    private val codepointsFileName: String,
    val fontFile: FontFile = FontFile(ttfFileName),
    val codePointsFile: CodePointsFile = CodePointsFile(codepointsFileName)
)

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