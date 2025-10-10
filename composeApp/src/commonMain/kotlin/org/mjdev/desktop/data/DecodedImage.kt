package org.mjdev.desktop.data

import androidx.compose.ui.graphics.ImageBitmap

data class DecodedImage(
    val bitmap: ImageBitmap,
    val width: Int,
    val height: Int
)
