package org.psippr.desktop.managers.notifications

/**
 * One notification of the desktop: shown as a popup when it arrives, then kept in the history on
 * the control center's home tab until dismissed.
 *
 * @property id unique id (UUID)
 * @property title short headline, e.g. the device or app it is about
 * @property message what happened
 * @property createdAt creation time in epoch milliseconds
 * @property dismissed true once dismissed or expired; never deleted, only flagged
 * @property actions buttons of a notification that asks something, empty for plain ones
 * @property checkbox label of its checkbox (e.g. "Remember device"), empty for none
 */
data class DesktopNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
    val dismissed: Boolean = false,
    val actions: List<NotificationAction> = emptyList(),
    val checkbox: String = "",
)
