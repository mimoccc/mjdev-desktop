package org.mjdev.desktop.helpers.image

import androidx.compose.ui.graphics.ImageBitmap

/** decodes an encoded image (png/jpg bytes) into a compose ImageBitmap */
expect fun ByteArray.decodeToImageBitmap(): ImageBitmap?
