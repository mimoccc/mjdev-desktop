package org.psippr.desktop.helpers

import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.psippr.desktop.data.DecodedImage

actual fun decodeImage(bytes: ByteArray): DecodedImage? = try {
    val skiaImage = Image.makeFromEncoded(bytes)
    val bitmap = skiaImage.toComposeImageBitmap()
    DecodedImage(bitmap, skiaImage.width, skiaImage.height)
} catch (e: Exception) {
    e.printStackTrace()
    null
}
