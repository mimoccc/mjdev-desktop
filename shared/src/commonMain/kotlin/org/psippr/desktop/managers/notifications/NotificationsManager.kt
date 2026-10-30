package org.psippr.desktop.managers.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.psippr.desktop.context.IDesktopContext
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * [INotificationsManager] of the desktop: history in memory as Compose state, stored by
 * [NotificationStore] on every change. Entries older than [NotificationRules.RETENTION_MS] are
 * flagged dismissed at start; nothing is ever deleted.
 */
@Suppress("unused")
class NotificationsManager(
    context: IDesktopContext,
) : INotificationsManager {
    private val store = NotificationStore(context.currentUser)
    private val items = mutableStateListOf<DesktopNotification>()

    override var popup: DesktopNotification? by mutableStateOf(null)
        private set

    override val visible: List<DesktopNotification>
        get() = NotificationRules.visible(items)

    init {
        items.addAll(NotificationRules.expire(store.load().items, now()))
        save()
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun notify(
        title: String,
        message: String,
    ): DesktopNotification {
        val notification =
            DesktopNotification(
                id = Uuid.random().toString(),
                title = title,
                message = message,
                createdAt = now(),
            )
        items.add(notification)
        popup = notification
        save()
        return notification
    }

    override fun dismiss(id: String) {
        val updated = NotificationRules.dismiss(items, id)
        items.clear()
        items.addAll(updated)
        if (popup?.id == id) popup = null
        save()
    }

    override fun hidePopup() {
        popup = null
    }

    private fun save() = store.save(NotificationHistory(items.toMutableList()))

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()
}
