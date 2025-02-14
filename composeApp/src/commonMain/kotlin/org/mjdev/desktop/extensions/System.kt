package org.mjdev.desktop.extensions

import kotlinx.datetime.Clock

object System {
    fun currentTimeMillis() = Clock.System.now().toEpochMilliseconds()
}