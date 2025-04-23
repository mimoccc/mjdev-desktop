package org.mjdev.desktop.managers.ai.actions

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.mjdev.desktop.managers.ai.actions.base.ActionsProvider

val ActionsProvider.ActionCurrentTime
    get() = action(
        "current time"
    ) {
        val timeZone = TimeZone.currentSystemDefault()
        val localDateTime = kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(timeZone)
        val hours = localDateTime.hour.toString().padStart(2, '0')
        val minutes = localDateTime.minute.toString().padStart(2, '0')
        val seconds = localDateTime.second.toString().padStart(2, '0')
        success("Current time is: $hours hours $minutes minutes $seconds seconds.")
    }
