package org.mjdev.desktop.helpers.system.meminfo

open class MemInfoStub {
    open val free: Double = 100.0
    open val total: Double = 100.0

    val used: Double
        get() = total - free
}