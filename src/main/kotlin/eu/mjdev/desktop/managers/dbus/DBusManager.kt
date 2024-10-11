/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.dbus

// todo
@Suppress("unused")
class DBusManager {
//    val connection by lazy { DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION) }
//    val settingsInterface by lazy {
//        connection.getRemoteObject(
//            "org.freedesktop.portal.Desktop",
//            "/org/freedesktop/portal/desktop",
//            SettingsInterface::class.java
//        )
//    }
//
//    val currentColorScheme get() = settingsInterface.Read("org.freedesktop.appearance", "color-scheme")
//
//    init {
//        println(recursiveVariantValue(currentColorScheme))
//    }
//
//    fun recursiveVariantValue(variant: Variant<*>): Any {
//        val value = variant.value
//        if (value !is Variant<*>) return value
//        else return recursiveVariantValue(value)
//    }
//
//    @DBusInterfaceName("org.freedesktop.portal.Settings")
//    interface SettingsInterface : DBusInterface {
//        fun Read(namespace: String, key: String): Variant<*>
//    }
}