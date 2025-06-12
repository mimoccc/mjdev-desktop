package org.mjdev.desktop.extensions

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.request.ImageRequest
import coil3.toBitmap
import okio.Path
import org.mjdev.desktop.context.IDesktopContext

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object ImageBitmapExt {
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
                imageLoader?.execute(req)?.image?.toBitmap()?.asImageBitmap()
            }
    }.getOrNull()

}
