package org.psippr.desktop.managers.notifications

import org.psippr.desktop.managers.base.IDelegate

/**
 * Desktop notifications: [notify] shows a popup and adds the entry to the history the control
 * center's home tab lists. Both lists are observable, so the UI follows them.
 */
interface INotificationsManager : IDelegate {
    /** History entries not dismissed, newest first. */
    val visible: List<DesktopNotification>

    /** The newest notification while its popup is due, null otherwise. */
    val popup: DesktopNotification?

    /** Adds a notification and shows its popup; returns it. */
    fun notify(
        title: String,
        message: String,
    ): DesktopNotification

    /** Flags the entry [id] dismissed (it stays stored). */
    fun dismiss(id: String)

    /** Hides the popup; the entry stays in the history. */
    fun hidePopup()

    companion object {
        /** No-op fallback used when no platform manager is available. */
        val EMPTY =
            object : INotificationsManager {
                override val visible: List<DesktopNotification> = emptyList()
                override val popup: DesktopNotification? = null

                override fun notify(
                    title: String,
                    message: String,
                ): DesktopNotification = DesktopNotification(title = title, message = message)

                override fun dismiss(id: String) {}

                override fun hidePopup() {}
            }
    }
}
