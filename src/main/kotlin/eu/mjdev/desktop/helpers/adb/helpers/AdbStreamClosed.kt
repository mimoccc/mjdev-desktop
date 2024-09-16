package eu.mjdev.desktop.helpers.adb.helpers

import java.io.IOException

internal class AdbStreamClosed(
    localId: Int
) : IOException(String.format("ADB stream is closed for localId: %x", localId))