package org.mjdev.desktop.helpers.gif

import okio.Path
import okio.Path.Companion.toPath

// Same convention as DesktopFile.correctDir / Log: /var/tmp/mjdev-desktop/... (persists across reboots).
actual fun gifCacheBaseDir(): Path = "/var/tmp/mjdev-desktop/gif-cache".toPath()
