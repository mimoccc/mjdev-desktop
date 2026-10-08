package org.psippr.desktop.helpers.gif

import okio.Path
import okio.Path.Companion.toPath

// Same convention as DesktopFile.correctDir / Log: /var/tmp/psippr-desktop/... (persists across reboots).
actual fun gifCacheBaseDir(): Path = "/var/tmp/psippr-desktop/gif-cache".toPath()
