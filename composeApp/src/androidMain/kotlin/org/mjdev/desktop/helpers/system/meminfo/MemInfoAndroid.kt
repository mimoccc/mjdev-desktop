package org.mjdev.desktop.helpers.system.meminfo

import okio.Path.Companion.toPath
import org.mjdev.desktop.extensions.PathExt.lines

class MemInfoAndroid: MemInfoStub() {
    private val runtime = Runtime.getRuntime()

    override val free: Double = runtime.freeMemory().toDouble().times(KB)

    override val total: Double = runtime.totalMemory().toDouble().times(KB)

    companion object {
        const val KB = 1024
    }
}