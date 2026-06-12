package org.mjdev.desktop.managers.ai.actions

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.mjdev.desktop.extensions.System.currentTime
import org.mjdev.desktop.managers.ai.actions.base.ActionsProvider
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val ActionsProvider.ActionCurrentTime
    get() = action(
        "current time"
    ) {
        val timeZone = TimeZone.currentSystemDefault()
        val localDateTime = currentTime.toLocalDateTime(timeZone)
        val hours = localDateTime.hour.toString().padStart(2, '0')
        val minutes = localDateTime.minute.toString().padStart(2, '0')
        val seconds = localDateTime.second.toString().padStart(2, '0')
        success("Current time is: $hours hours $minutes minutes $seconds seconds.")
    }
