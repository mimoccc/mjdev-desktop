package org.mjdev.desktop.extensions

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Builds an [ImageBitmap] from packed ARGB_8888 pixels in a SINGLE bulk operation.
 * Replaces the per-pixel `Canvas.drawRect` path of [writePixels] (which issued one draw call per
 * pixel — ~2M calls for a 1080p frame) on the hot GIF-decode / palette paths.
 * Desktop uses Skia `Bitmap.installPixels`, Android uses `Bitmap.createBitmap`.
 */
expect fun imageBitmapFromArgb(
    pixels: IntArray,
    width: Int,
    height: Int,
): ImageBitmap
