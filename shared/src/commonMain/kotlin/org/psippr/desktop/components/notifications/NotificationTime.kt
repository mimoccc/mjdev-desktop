package org.psippr.desktop.components.notifications

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Formats the time of a notification: `14:05` today, `30.10. 14:05` on another day. */
object NotificationTime {
    /** Time of [epochMs] as seen at [nowMs], in [zone]. */
    fun format(
        epochMs: Long,
        nowMs: Long,
        zone: TimeZone = TimeZone.currentSystemDefault(),
    ): String {
        val time = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(zone)
        val now = Instant.fromEpochMilliseconds(nowMs).toLocalDateTime(zone)
        val clock = "${two(time.hour)}:${two(time.minute)}"
        return if (time.date == now.date) clock else "${time.day}.${time.month.number}. $clock"
    }

    private fun two(value: Int): String = value.toString().padStart(2, '0')
}
