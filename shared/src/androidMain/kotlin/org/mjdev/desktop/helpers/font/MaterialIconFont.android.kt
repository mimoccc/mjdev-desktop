package org.mjdev.desktop.helpers.font

import androidx.compose.ui.text.font.FontFamily
import org.mjdev.desktop.helpers.fuzzywuzzy.FuzzySearch
import org.mjdev.desktop.interfaces.IFont

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MaterialIconFont actual constructor(
    private val ttfFileName: String,
    private val codepointsFileName: String,
) : IFont {
    private val fontFile: FontDescriptor by lazy {
        FontDescriptor(ttfFileName)
    }
    private val codePointsFile: CodePointsFile by lazy {
        CodePointsFile(codepointsFileName)
    }

    override val fontFamily: FontFamily
        get() = fontFile.fontFamily

    private val cache: MutableMap<String, Int> = mutableMapOf()

    override fun iconForName(iconName: String?): Int? =
        runCatching {
            if (iconName != null) {
                if (cache.containsKey(iconName)) {
                    cache[iconName]
                } else {
                    codePointsFile.icons
                        .map { icon ->
                            Pair(FuzzySearch.ratio(iconName, icon.key), icon)
                        }.maxByOrNull { it.first }
                        ?.second
                        ?.value
                        ?.also {
                            cache[iconName] = it
                        }
                }
            } else {
                null
            }
        }.getOrNull()
}
