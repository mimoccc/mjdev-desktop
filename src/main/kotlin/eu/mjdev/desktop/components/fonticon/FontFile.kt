package eu.mjdev.desktop.components.fonticon

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Typeface
import eu.mjdev.desktop.helpers.ResourceStream
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontVariation
import org.jetbrains.skia.Typeface

class FontFile(
    private val ttfFileName: String,
    private val ttfResource: ResourceStream = ResourceStream(ttfFileName),
    private val fontWeight: FontWeight = FontWeight.Normal,
    val fontFamily: FontFamily = Typeface.makeFromData(Data.makeFromBytes(ttfResource.bytes))
        .makeClone(fontWeight).let {
            FontFamily(typeface = Typeface(it, "alias${fontWeight.weight}"))
        }
) {
    companion object {
        fun Typeface.makeClone(weight: FontWeight): Typeface {
            return makeClone(FontVariation("wght", weight.weight.toFloat()))
        }
    }
}