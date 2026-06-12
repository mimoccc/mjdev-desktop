package org.mjdev.desktop.helpers.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun ByteArray.decodeToImageBitmap(): ImageBitmap? = runCatching {
    Image.makeFromEncoded(this).toComposeImageBitmap()
}.getOrNull()
