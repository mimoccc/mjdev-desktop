package org.mjdev.desktop.helpers.font

import androidx.compose.ui.text.font.FontFamily
import org.mjdev.desktop.interfaces.IFont

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MaterialIconFont actual constructor(
    ttfFileName: String,
    codepointsFileName: String,
) : IFont {
    override val fontFamily: FontFamily
        get() = FontFamily.Default

    override fun iconForName(iconName: String?): Int? = null
}