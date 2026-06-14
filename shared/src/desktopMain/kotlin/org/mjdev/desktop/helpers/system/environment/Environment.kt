package org.mjdev.desktop.helpers.system.environment

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

@Suppress("FunctionName")
fun Environment() =
    when (hostOs) {
        // todo other platforms
        OS.Linux -> EnvironmentLinux()
        else -> EnvironmentStub()
    }
