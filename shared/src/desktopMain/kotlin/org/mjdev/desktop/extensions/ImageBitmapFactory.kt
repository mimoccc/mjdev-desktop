package org.mjdev.desktop.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo

// Skia N32 (BGRA on little-endian) layout: the little-endian bytes of a 0xAARRGGBB int are
// already [B, G, R, A], so the packed ARGB pixels map straight onto the byte buffer.
actual fun imageBitmapFromArgb(
    pixels: IntArray,
    width: Int,
    height: Int,
): ImageBitmap {
    if (width <= 0 || height <= 0) return ImageBitmap(1, 1)
    val bytes = ByteArray(width * height * 4)
    for (i in 0 until minOf(pixels.size, width * height)) {
        val p = pixels[i]
        val o = i * 4
        bytes[o] = p.toByte() // B
        bytes[o + 1] = (p shr 8).toByte() // G
        bytes[o + 2] = (p shr 16).toByte() // R
        bytes[o + 3] = (p shr 24).toByte() // A
    }
    val bitmap = Bitmap()
    bitmap.allocPixels(ImageInfo.makeN32(width, height, ColorAlphaType.UNPREMUL))
    bitmap.installPixels(bytes)
    return bitmap.asComposeImageBitmap()
}
