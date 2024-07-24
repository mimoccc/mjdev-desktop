@file:Suppress("unused")

package eu.mjdev.desktop.components.fonticon

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.helpers.ResourceStream
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Typeface
import androidx.compose.ui.text.style.TextAlign
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.LocalDesktop
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontVariation
import org.jetbrains.skia.Typeface

@Composable
fun MaterialIcon(
    api: DesktopProvider = LocalDesktop.current,
    iconicFont: MaterialIconFont = api.currentUser.theme.iconSet,
    iconId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color = Color.Unspecified
) {
    Text(
        text = iconId.toChar().toString(),
        modifier = modifier,
        color = tint,
        fontSize = size.value.sp,
        fontFamily = iconicFont.fontFile.fontFamily,
        textAlign = TextAlign.Center
    )
}

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

class MaterialIconFont(
    private val ttfFileName: String,
    private val codepointsFileName: String,
    val fontFile: FontFile = FontFile(ttfFileName),
    val codePointsFile: CodePointsFile = CodePointsFile(codepointsFileName)
)

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

class CodePointsFile(
    private val codepointsFileName: String,
    private val codepointsResourceResource: ResourceStream = ResourceStream(codepointsFileName),
    val icons: Map<String, Int> = codepointsResourceResource.string.split("\n")
        .map { it.split(" ") }
        .mapNotNull { if (it.size == 2) Pair(it[0], it[1].toInt(radix = 16)) else null }
        .toMap()
)
