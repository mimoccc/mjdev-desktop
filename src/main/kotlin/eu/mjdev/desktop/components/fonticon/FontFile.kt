package eu.mjdev.desktop.components.fonticon

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import eu.mjdev.desktop.helpers.ResourceStream

class FontFile(
    private val ttfFileName: String,
    private val ttfResource: ResourceStream = ResourceStream(ttfFileName),
    private val fontWeight: FontWeight = FontWeight.Normal,
    private val font: Font = Font(ttfFileName, ttfResource.bytes, fontWeight),
    val fontFamily: FontFamily = FontFamily(font)
)
