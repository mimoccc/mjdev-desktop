package org.mjdev.desktop.helpers.gif

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.jvm.JvmField

class GifFrame(
    @JvmField val image: ImageBitmap?,
    @JvmField val delay: Int
)