package org.mjdev.desktop.helpers

import org.mjdev.desktop.data.DecodedImage

expect fun decodeImage(bytes: ByteArray): DecodedImage?
