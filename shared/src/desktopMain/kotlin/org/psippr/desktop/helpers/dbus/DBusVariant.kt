package org.psippr.desktop.helpers.dbus

/**
 * A D-Bus variant: a value together with its own type signature.
 *
 * @property signature type of [value], e.g. `s`
 * @property value the decoded value
 */
data class DBusVariant(
    val signature: String,
    val value: Any?,
)
