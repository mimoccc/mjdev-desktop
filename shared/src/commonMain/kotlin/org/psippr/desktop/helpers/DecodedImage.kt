package org.psippr.desktop.helpers

import org.psippr.desktop.data.DecodedImage

expect fun decodeImage(bytes: ByteArray): DecodedImage?
