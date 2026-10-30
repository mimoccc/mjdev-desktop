package org.psippr.desktop.helpers.dbus

import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The small D-Bus codec reads back exactly what it writes, including variants and dictionaries. */
class DBusCodecTest {
    /** A Notify call as notify-send sends it: susssasa{sv}i with a hints dictionary of variants. */
    @Test
    fun notifyCallRoundTrips() {
        val call =
            DBusMessage(
                type = DBusMessage.TYPE_METHOD_CALL,
                serial = 7,
                path = "/org/freedesktop/Notifications",
                interfaceName = "org.freedesktop.Notifications",
                member = "Notify",
                destination = "org.freedesktop.Notifications",
                signature = "susssasa{sv}i",
                body =
                    listOf(
                        "notify-send",
                        0L,
                        "",
                        "Backup done",
                        "1 240 files copied",
                        listOf("default", "Open"),
                        mapOf("urgency" to DBusVariant("y", 1.toByte()), "sender-pid" to DBusVariant("x", 4242L)),
                        -1,
                    ),
            )
        val decoded = DBusCodec.decode(ByteArrayInputStream(DBusCodec.encode(call)))
        assertEquals(call, decoded)
    }

    @Test
    fun replyCarriesItsSerial() {
        val reply =
            DBusMessage(
                type = DBusMessage.TYPE_METHOD_RETURN,
                serial = 2,
                replySerial = 7,
                signature = "u",
                body = listOf(5L),
            )
        val decoded = DBusCodec.decode(ByteArrayInputStream(DBusCodec.encode(reply)))
        assertEquals(7L, decoded.replySerial)
        assertEquals(listOf<Any?>(5L), decoded.body)
        assertNull(decoded.member)
    }

    @Test
    fun signaturesSplitIntoSingleTypes() {
        assertEquals(listOf("s", "u", "s", "s", "s", "as", "a{sv}", "i"), DBusSignature.split("susssasa{sv}i"))
        assertEquals(listOf("a(yv)", "(s(ii))"), DBusSignature.split("a(yv)(s(ii))"))
    }

    @Test
    fun sessionBusAddressGivesItsSocket() {
        assertEquals("/run/user/1000/bus", DBusAddress.socketPath("unix:path=/run/user/1000/bus,guid=abc").toString())
        assertNull(DBusAddress.socketPath("unix:abstract=/tmp/dbus-x"))
        assertNull(DBusAddress.socketPath(null))
    }
}
