package org.psippr.desktop.managers.notifications

/**
 * Persisted notification history (JSON).
 *
 * @property items every notification ever made, dismissed ones flagged, never removed
 */
data class NotificationHistory(
    val items: MutableList<DesktopNotification> = mutableListOf(),
)
