package eu.mjdev.desktop.extensions

import androidx.compose.ui.graphics.*
import eu.mjdev.desktop.extensions.ColorUtils.nonAlphaValue
import eu.mjdev.desktop.helpers.bitmap.Bitmap
import org.jetbrains.skiko.toBufferedImage

typealias SkiaBitmap = org.jetbrains.skia.Bitmap

@Suppress("MemberVisibilityCanBePrivate", "unused")
object BitmapUtils {

    fun SkiaBitmap.toBitmap() = Bitmap(toBufferedImage())

    fun ImageBitmap.toBitmap() = Bitmap(asSkiaBitmap().toBufferedImage())

    val Bitmap.topMostColor
        get() = createScaledBitmap(this, 1, 1).getPixel(0, 0)

    val SkiaBitmap.topMostColor
        get() = toBitmap().topMostColor

    val ImageBitmap.topMostColor
        get() = asSkiaBitmap().topMostColor

    val Bitmap.maxLightColor
        get() = allPixels.maxBy { it.nonAlphaValue }

    val SkiaBitmap.maxLightColor
        get() = toBitmap().topMostColor

    val ImageBitmap.maxLightColor: Color
        get() = asSkiaBitmap().topMostColor

    val Bitmap.maxDarkColor
        get() = allPixels.minBy { it.nonAlphaValue }

    val SkiaBitmap.maxDarkColor
        get() = toBitmap().topMostColor

    val ImageBitmap.maxDarkColor: Color
        get() = asSkiaBitmap().topMostColor

    fun ImageBitmap.cut(x: Int, y: Int, w: Int, h: Int): ImageBitmap =
        toBitmap().cut(x, y, w, h).image.toComposeImageBitmap()

}