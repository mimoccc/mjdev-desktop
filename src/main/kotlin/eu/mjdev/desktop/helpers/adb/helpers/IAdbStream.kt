package eu.mjdev.desktop.helpers.adb.helpers

import okio.BufferedSink
import okio.BufferedSource

interface IAdbStream : AutoCloseable {
    val source: BufferedSource
    val sink: BufferedSink
}
