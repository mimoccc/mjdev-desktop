package org.mjdev.desktop.extensions

import androidx.compose.ui.graphics.ImageBitmap
import org.mjdev.desktop.context.IDesktopContext

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object ImageBitmapExt {

    fun ImageBitmap.cut(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): ImageBitmap

    suspend fun IDesktopContext.loadPicture(
        src: Any?
    ): ImageBitmap?
}


