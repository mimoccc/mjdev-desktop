package org.psippr.desktop.helpers.dbus

/**
 * One D-Bus message.
 *
 * @property type one of the TYPE_ constants
 * @property serial sender's number of the message, never 0
 * @property flags message flags (FLAG_ constants)
 * @property path object path of a call or signal
 * @property interfaceName interface of a call or signal
 * @property member method or signal name
 * @property errorName name of an error reply
 * @property replySerial serial of the call a reply answers
 * @property destination bus name the message goes to
 * @property sender bus name it came from (set by the bus)
 * @property signature types of [body]
 * @property body the arguments
 */
data class DBusMessage(
    val type: Int,
    val serial: Long,
    val flags: Int = 0,
    val path: String? = null,
    val interfaceName: String? = null,
    val member: String? = null,
    val errorName: String? = null,
    val replySerial: Long? = null,
    val destination: String? = null,
    val sender: String? = null,
    val signature: String = "",
    val body: List<Any?> = emptyList(),
) {
    companion object {
        const val TYPE_METHOD_CALL = 1
        const val TYPE_METHOD_RETURN = 2
        const val TYPE_ERROR = 3
        const val TYPE_SIGNAL = 4

        /** The caller does not want a reply. */
        const val FLAG_NO_REPLY_EXPECTED = 0x1
    }
}
