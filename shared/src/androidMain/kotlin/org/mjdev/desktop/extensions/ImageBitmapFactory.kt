package org.mjdev.desktop.extensions

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

// Bitmap.createBitmap consumes packed ARGB_8888 ints directly — a single bulk copy instead of
// the per-pixel Canvas.drawRect path.
actual fun imageBitmapFromArgb(
    pixels: IntArray,
    width: Int,
    height: Int,
): ImageBitmap {
    if (width <= 0 || height <= 0) {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888).asImageBitmap()
}
