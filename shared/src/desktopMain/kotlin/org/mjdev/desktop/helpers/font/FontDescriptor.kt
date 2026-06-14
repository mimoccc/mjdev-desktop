package org.mjdev.desktop.helpers.font

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.platform.Font
import org.mjdev.desktop.log.Log

class FontDescriptor(
    ttfFileName: String,
) {
    private val font: Font? =
        runCatching {
            Font(ttfFileName)
        }.onFailure { e ->
            Log.e(e)
        }.getOrNull()

    val fontFamily: FontFamily =
        font?.toFontFamily() ?: FontFamily.Default
}
