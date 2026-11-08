package org.psippr.desktop.managers.notifications

import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.psippr.desktop.helpers.dbus.DBusAddress
import org.psippr.desktop.helpers.dbus.DBusConnection
import org.psippr.desktop.helpers.dbus.DBusMessage
import org.psippr.desktop.log.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * The standard Linux notification service (`org.freedesktop.Notifications`, Desktop Notifications
 * Specification 1.2) on the session bus, so `notify-send` and every app using libnotify show
 * their notifications here: popup plus history on the control center's home tab.
 *
 * The name is only taken when free: under GNOME its own service keeps it; in the psippr session
 * and in nested runs (own bus) this one serves.
 */
class NotificationService(
    private val manager: INotificationsManager,
    private val scope: CoroutineScope,
) {
    private var connection: DBusConnection? = null
    private var job: Job? = null

    // the specification uses numeric ids; the history uses UUIDs
    private val nextId = AtomicLong(0)
    private val uuids = ConcurrentHashMap<Long, String>()

    /** Connects and takes the service name; false when there is no bus or the name is taken. */
    fun start(): Boolean {
        val path = DBusAddress.socketPath(System.getenv(DBusAddress.SESSION_ENV)) ?: return false
        val bus =
            runCatching { DBusConnection.open(path) }.getOrElse { e ->
                Log.w("NotificationService: no session bus: ${e.message}")
                return false
            }
        if (!bus.requestName(SERVICE)) {
            Log.i("NotificationService: $SERVICE is served by another program (e.g. GNOME), not taking it")
            bus.close()
            return false
        }
        connection = bus
        Log.i("NotificationService: serving $SERVICE as ${bus.uniqueName}")
        job = scope.launch(Dispatchers.IO) { serve(bus) }
        return true
    }

    /** Stops serving; the bus frees the name. */
    fun stop() {
        job?.cancel()
        connection?.close()
        connection = null
    }

    private fun serve(bus: DBusConnection) {
        runCatching {
            while (scope.isActive) {
                val message = bus.receive()
                if (message.type == DBusMessage.TYPE_METHOD_CALL && message.interfaceName == SERVICE) {
                    answer(bus, message)
                } else if (message.type == DBusMessage.TYPE_METHOD_CALL && message.interfaceName == PEER) {
                    reply(bus, message, "", emptyList())
                }
            }
        }.onFailure { e -> Log.w("NotificationService: stopped: ${e.message}") }
    }

    private fun answer(
        bus: DBusConnection,
        call: DBusMessage,
    ) = when (call.member) {
        NOTIFY -> reply(bus, call, "u", listOf(notify(call.body)))
        CLOSE -> close(bus, call)
        CAPABILITIES -> reply(bus, call, "as", listOf(CAPABILITY_LIST))
        SERVER_INFORMATION -> reply(bus, call, "ssss", listOf(SERVER_NAME, VENDOR, version(), SPEC_VERSION))
        else -> error(bus, call)
    }

    // Notify(s app_name, u replaces_id, s app_icon, s summary, s body, as actions, a{sv} hints, i timeout)
    private fun notify(body: List<Any?>): Long {
        val appName = body.getOrNull(ARG_APP_NAME) as String? ?: ""
        val summary = body.getOrNull(ARG_SUMMARY) as String? ?: ""
        val text = body.getOrNull(ARG_BODY) as String? ?: ""
        val notification = manager.notify(title = summary.ifBlank { appName }, message = text)
        // the change comes from this bus thread: tell Compose now, or the popup waits for the next input event
        Snapshot.sendApplyNotifications()
        val id = nextId.incrementAndGet()
        uuids[id] = notification.id
        return id
    }

    private fun close(
        bus: DBusConnection,
        call: DBusMessage,
    ) {
        val id = call.body.firstOrNull() as Long? ?: 0L
        uuids[id]?.let(manager::dismiss)
        Snapshot.sendApplyNotifications()
        reply(bus, call, "", emptyList())
        signal(bus, CLOSED, "uu", listOf(id, REASON_CLOSED_BY_CALL))
    }

    private fun reply(
        bus: DBusConnection,
        call: DBusMessage,
        signature: String,
        body: List<Any?>,
    ) {
        if (call.flags and DBusMessage.FLAG_NO_REPLY_EXPECTED != 0) return
        bus.send(
            DBusMessage(
                type = DBusMessage.TYPE_METHOD_RETURN,
                serial = bus.nextSerial(),
                replySerial = call.serial,
                destination = call.sender,
                signature = signature,
                body = body,
            ),
        )
    }

    private fun error(
        bus: DBusConnection,
        call: DBusMessage,
    ) = bus.send(
        DBusMessage(
            type = DBusMessage.TYPE_ERROR,
            serial = bus.nextSerial(),
            replySerial = call.serial,
            destination = call.sender,
            errorName = UNKNOWN_METHOD,
            signature = "s",
            body = listOf("No method ${call.member}"),
        ),
    )

    private fun signal(
        bus: DBusConnection,
        member: String,
        signature: String,
        body: List<Any?>,
    ) = bus.send(
        DBusMessage(
            type = DBusMessage.TYPE_SIGNAL,
            serial = bus.nextSerial(),
            path = OBJECT_PATH,
            interfaceName = SERVICE,
            member = member,
            signature = signature,
            body = body,
        ),
    )

    // jpackage launchers set the app version as a system property; empty in a plain Gradle run
    private fun version(): String = System.getProperty(VERSION_PROPERTY).orEmpty()

    private companion object {
        const val SERVICE = "org.freedesktop.Notifications"
        const val OBJECT_PATH = "/org/freedesktop/Notifications"
        const val PEER = "org.freedesktop.DBus.Peer"
        const val NOTIFY = "Notify"
        const val CLOSE = "CloseNotification"
        const val CAPABILITIES = "GetCapabilities"
        const val SERVER_INFORMATION = "GetServerInformation"
        const val CLOSED = "NotificationClosed"
        const val UNKNOWN_METHOD = "org.freedesktop.DBus.Error.UnknownMethod"

        /** What this server supports: plain body text. */
        val CAPABILITY_LIST = listOf("body")

        const val SERVER_NAME = "psippr-desktop"
        const val VENDOR = "psippr"
        const val SPEC_VERSION = "1.2"
        const val VERSION_PROPERTY = "jpackage.app-version"

        /** NotificationClosed reason: closed by a CloseNotification call. */
        const val REASON_CLOSED_BY_CALL = 3L

        const val ARG_APP_NAME = 0
        const val ARG_SUMMARY = 3
        const val ARG_BODY = 4
    }
}
