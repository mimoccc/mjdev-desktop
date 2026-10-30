package org.psippr.desktop.managers.notifications

/**
 * A button of a notification that asks something.
 *
 * @property id what the asker gets back when it is pressed
 * @property label text of the button
 */
data class NotificationAction(
    val id: String = "",
    val label: String = "",
)
