package org.mjdev.desktop.extensions

// import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.ImageBitmap
// import androidx.compose.ui.graphics.asSkiaBitmap
// import androidx.compose.ui.graphics.toComposeImageBitmap
// import org.jetbrains.skiko.toBufferedImage
// import org.mjdev.desktop.extensions.Colors.nonAlphaValue
// import org.mjdev.desktop.helpers.bitmap.Bitmap
// import java.awt.Image
// import java.awt.image.BufferedImage

// typealias SkiaBitmap = org.jetbrains.skia.Bitmap

// @Suppress("MemberVisibilityCanBePrivate", "unused")
// object BitmapUtils {

//    fun SkiaBitmap.toBitmap() = Bitmap(toBufferedImage())
//
//    fun ImageBitmap.toBitmap() = Bitmap(asSkiaBitmap().toBufferedImage())

//    val Bitmap.topMostColor
//        get() = createScaledBitmap(this, 1, 1).getPixel(0, 0)

//    val SkiaBitmap.topMostColor
//        get() = toBitmap().topMostColor

//    val ImageBitmap.topMostColor
//        get() = asSkiaBitmap().topMostColor

//    val Bitmap.maxLightColor
//        get() = allPixels.maxBy { it.nonAlphaValue }

//    val SkiaBitmap.maxLightColor
//        get() = toBitmap().topMostColor

//    val ImageBitmap.maxLightColor: Color
//        get() = asSkiaBitmap().topMostColor

//    val Bitmap.maxDarkColor
//        get() = allPixels.minBy { it.nonAlphaValue }

//    val SkiaBitmap.maxDarkColor
//        get() = toBitmap().topMostColor

//    val ImageBitmap.maxDarkColor: Color
//        get() = asSkiaBitmap().topMostColor

//    fun ImageBitmap.cut(x: Int, y: Int, w: Int, h: Int): ImageBitmap =
//        toBitmap().cut(x, y, w, h).image.toComposeImageBitmap()

//    fun Image.toBufferedImage(): BufferedImage {
//        if (this is BufferedImage) {
//            return this
//        }
//        val bimage = BufferedImage(getWidth(null), getHeight(null), BufferedImage.TYPE_INT_ARGB)
//        val bGr = bimage.createGraphics()
//        bGr.drawImage(this, 0, 0, null)
//        bGr.dispose()
//        return bimage
//    }
//
// }
