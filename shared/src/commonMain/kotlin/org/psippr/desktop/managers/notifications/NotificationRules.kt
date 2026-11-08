package org.psippr.desktop.managers.notifications

/**
 * Pure rules of the notification history: what is listed, and when an entry expires. An entry
 * is never removed: dismissing or expiring only sets its flag.
 */
object NotificationRules {
    /** Age after which an entry is flagged dismissed by itself (30 days, as all events). */
    const val RETENTION_MS: Long = 30L * 24 * 60 * 60 * 1000

    /** Entries the history lists: not dismissed, newest first. */
    fun visible(items: List<DesktopNotification>): List<DesktopNotification> = items
        .filterNot { it.dismissed }
        .sortedByDescending { it.createdAt }

    /** [items] with every entry older than [RETENTION_MS] at [now] flagged dismissed. */
    fun expire(
        items: List<DesktopNotification>,
        now: Long,
    ): List<DesktopNotification> = items.map { item ->
        if (!item.dismissed && now - item.createdAt > RETENTION_MS) item.copy(dismissed = true) else item
    }

    /** [items] with the entry [id] flagged dismissed. */
    fun dismiss(
        items: List<DesktopNotification>,
        id: String,
    ): List<DesktopNotification> = items.map { item -> if (item.id == id) item.copy(dismissed = true) else item }
}
