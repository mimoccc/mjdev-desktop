package org.mjdev.desktop.extensions

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant

@OptIn(ExperimentalTime::class)
object System {

    val currentTimeMillis
        get() = Clock.System.now().toEpochMilliseconds()

    val currentTime
        get() = Instant.fromEpochMilliseconds(currentTimeMillis)

}