package org.psippr.desktop.managers.notifications

import kotlinx.datetime.TimeZone
import org.psippr.desktop.components.notifications.NotificationTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Regression tests: action results are notifications with a history, never deleted, only flagged. */
class NotificationRulesTest {
    private val older = DesktopNotification(id = "a", title = "Phone", message = "Sent", createdAt = 1_000L)
    private val newer = DesktopNotification(id = "b", title = "Phone", message = "Saved", createdAt = 2_000L)

    @Test
    fun historyListsNewestFirstWithoutDismissed() {
        val items = listOf(older, newer, older.copy(id = "c", dismissed = true))
        assertEquals(listOf("b", "a"), NotificationRules.visible(items).map { it.id })
    }

    /** Dismissing keeps the entry (flag only), as nothing may be deleted. */
    @Test
    fun dismissOnlyFlags() {
        val items = NotificationRules.dismiss(listOf(older, newer), "a")
        assertEquals(2, items.size)
        assertTrue(items.single { it.id == "a" }.dismissed)
        assertEquals(listOf("b"), NotificationRules.visible(items).map { it.id })
    }

    @Test
    fun entriesOlderThanThirtyDaysExpire() {
        val now = older.createdAt + NotificationRules.RETENTION_MS + 1
        val items = NotificationRules.expire(listOf(older, newer.copy(createdAt = now - 1)), now)
        assertEquals(2, items.size)
        assertEquals(listOf(true, false), items.map { it.dismissed })
    }

    @Test
    fun timeShowsClockTodayAndDateOtherwise() {
        val utc = TimeZone.UTC
        val today = 1_793_383_200_000L // 30.10.2026 18:00 UTC
        assertEquals("18:00", NotificationTime.format(today, today + MINUTE_MS, utc))
        assertEquals("29.10. 18:00", NotificationTime.format(today - DAY_MS, today, utc))
    }

    private companion object {
        const val MINUTE_MS = 60_000L
        const val DAY_MS = 24 * 60 * 60_000L
    }
}
