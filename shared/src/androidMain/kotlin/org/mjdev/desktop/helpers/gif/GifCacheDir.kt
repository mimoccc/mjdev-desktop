package org.mjdev.desktop.helpers.gif

import okio.Path
import okio.Path.Companion.toPath

// Animated GIF wallpapers are a desktop feature; on Android we just point at the JVM temp dir so
// the shared code compiles and still works if ever used.
actual fun gifCacheBaseDir(): Path =
    (System.getProperty("java.io.tmpdir") ?: "/tmp")
        .trimEnd('/')
        .plus("/mjdev-desktop/gif-cache")
        .toPath()
