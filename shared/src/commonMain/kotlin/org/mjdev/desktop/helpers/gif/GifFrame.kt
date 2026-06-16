package org.mjdev.desktop.helpers.gif

import okio.Path
import kotlin.jvm.JvmField

// A decoded frame is NOT kept in RAM — only its on-disk cache file (raw ARGB pixels) + delay.
// The bitmap is built lazily from [file] on demand (see GifDecoder.getFrame) with a small LRU.
class GifFrame(
    @JvmField val delay: Int,
    @JvmField val file: Path,
)
