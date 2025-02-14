package org.mjdev.desktop.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import coil3.request.ImageRequest
import coil3.toBitmap
import okio.Path
import org.jetbrains.skiko.toBufferedImage
import org.mjdev.desktop.interfaces.IDesktopContext

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object ImageBitmapExt {

    actual fun ImageBitmap.cut(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): ImageBitmap = asSkiaBitmap().toBufferedImage().apply {
        getSubimage(x, y, width, height)
    }.toComposeImageBitmap()

    actual suspend fun IDesktopContext.loadPicture(
        src: Any?
    ): ImageBitmap? = runCatching {
        ImageRequest.Builder(platformContext!!)
            .data(
                when (src) {
                    is Path -> src.toFile()
                    else -> src.toString()
                }
            )
            .build().let { req ->
                imageLoader?.execute(req)?.image?.toBitmap()?.asComposeImageBitmap()
            }
    }.getOrNull()

}
