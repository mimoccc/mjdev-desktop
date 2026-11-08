package org.psippr.desktop.helpers

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import org.psippr.desktop.data.DecodedImage

actual fun decodeImage(bytes: ByteArray): DecodedImage? = try {
    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val imageBitmap = bmp.asImageBitmap()
    DecodedImage(imageBitmap, bmp.width, bmp.height)
} catch (e: Exception) {
    e.printStackTrace()
    null
}
