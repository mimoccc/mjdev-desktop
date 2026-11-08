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

    /**
     * Shows a notification that asks something: [actions] as buttons and, when [checkbox] is not
     * empty, a checkbox. The pressed action and the checkbox state go to [onAnswer]; then the entry
     * is dismissed. Its popup stays until it is answered.
     */
    fun ask(
        title: String,
        message: String,
        actions: List<NotificationAction>,
        checkbox: String = "",
        onAnswer: (actionId: String, checked: Boolean) -> Unit,
    ): DesktopNotification = notify(title, message)

    /** Answers the question [id] with the action [actionId] and the checkbox state [checked]. */
    fun answer(
        id: String,
        actionId: String,
        checked: Boolean,
    ) {}

    /** True while [id] asks something that can still be answered (its asker is waiting). */
    fun isOpen(id: String): Boolean = false

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
