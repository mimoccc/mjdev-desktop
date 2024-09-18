package eu.mjdev.desktop.extensions

import androidx.compose.ui.graphics.*
import eu.mjdev.desktop.helpers.bitmap.Bitmap
import org.jetbrains.skiko.toBufferedImage

typealias  SkiaBitmap = org.jetbrains.skia.Bitmap

@Suppress("MemberVisibilityCanBePrivate", "unused")
object BitmapUtils {

    fun SkiaBitmap.toBitmap() = Bitmap(toBufferedImage())

    fun Bitmap.getTopMostColor(): Color = createScaledBitmap(this, 1, 1).getPixel(0, 0)

    fun SkiaBitmap.getTopMostColor(): Color = toBitmap().getTopMostColor()

    fun ImageBitmap.getTopMostColor(): Color = asSkiaBitmap().getTopMostColor()
}