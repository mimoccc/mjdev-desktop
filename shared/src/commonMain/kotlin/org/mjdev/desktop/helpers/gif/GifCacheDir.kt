package org.mjdev.desktop.helpers.gif

import okio.Path

/**
 * Base directory for the persistent on-disk GIF frame cache. Mirrors the project's
 * `/var/tmp/mjdev-desktop/...` convention (same as corrected-desktop-files): survives across
 * app restarts so a GIF is decoded only once.
 */
expect fun gifCacheBaseDir(): Path
