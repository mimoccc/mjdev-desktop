package org.mjdev.desktop.extensions

import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object System {
    val currentTimeMillis
        get() = Clock.System.now().toEpochMilliseconds()

    val currentTime
        get() = Instant.fromEpochMilliseconds(currentTimeMillis)
}
