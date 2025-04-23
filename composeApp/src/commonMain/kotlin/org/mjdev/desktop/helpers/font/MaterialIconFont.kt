package org.mjdev.desktop.helpers.font

import org.mjdev.desktop.interfaces.IFont

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MaterialIconFont(
    ttfFileName: String,
    codepointsFileName: String,
) : IFont

val MaterialSymbolsOutlined = MaterialIconFont(
    "icons/MaterialSymbolsOutlined.ttf",
    "icons/MaterialSymbolsOutlined.codepoints"
)

val MaterialSymbolsRounded = MaterialIconFont(
    "icons/MaterialSymbolsRounded.ttf",
    "icons/MaterialSymbolsRounded.codepoints"
)

val MaterialSymbolsSharp = MaterialIconFont(
    "icons/MaterialSymbolsSharp.ttf",
    "icons/MaterialSymbolsSharp.codepoints"
)