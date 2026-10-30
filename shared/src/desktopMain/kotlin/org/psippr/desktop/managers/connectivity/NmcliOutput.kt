package org.psippr.desktop.managers.connectivity

import org.psippr.desktop.data.Ipv4Settings
import org.psippr.desktop.data.Ipv4Validator

/** Reads `nmcli -t` (terse) answers and builds the arguments that change IPv4 settings. */
object NmcliOutput {
    /** nmcli's device type of wired adapters. */
    private const val TYPE_ETHERNET = "ethernet"

    /** nmcli's state of a device it leaves alone (e.g. Docker's virtual adapters). */
    private const val STATE_UNMANAGED = "unmanaged"

    /** nmcli's value of an empty field. */
    private const val EMPTY = "--"

    /** Terse field separator, and how a separator inside a value is escaped. */
    private const val SEPARATOR = ':'
    private const val ESCAPED_SEPARATOR = "\\:"

    private const val METHOD = "ipv4.method"
    private const val ADDRESSES = "ipv4.addresses"
    private const val GATEWAY = "ipv4.gateway"
    private const val DNS = "ipv4.dns"
    private const val AUTOCONNECT = "connection.autoconnect"

    /** Fields to ask `nmcli connection show` for, read by [ipv4]. */
    const val IPV4_FIELDS = "$METHOD,$ADDRESSES,$GATEWAY,$DNS,$AUTOCONNECT"

    private const val METHOD_AUTO = "auto"
    private const val METHOD_MANUAL = "manual"
    private const val YES = "yes"
    private const val NO = "no"

    /**
     * Wired adapters NetworkManager manages, from `nmcli -t -f DEVICE,TYPE,STATE device`. The
     * kernel has no `ethernet` folder for them in sysfs, so asking NetworkManager is reliable.
     */
    fun ethernetDevices(lines: List<String>): List<String> = lines
        .map { line -> line.split(SEPARATOR) }
        .filter { fields -> fields.size >= 3 && fields[1] == TYPE_ETHERNET && fields[2] != STATE_UNMANAGED }
        .map { fields -> fields[0] }

    /** `KEY:value` lines of a terse answer as a map; empty values (`--`) left out. */
    fun fields(lines: List<String>): Map<String, String> = lines
        .mapNotNull { line ->
            val key = line.substringBefore(SEPARATOR)
            val value = line.substringAfter(SEPARATOR, "").replace(ESCAPED_SEPARATOR, SEPARATOR.toString()).trim()
            if (key.isBlank() || value.isBlank() || value == EMPTY) null else key to value
        }.toMap()

    /** IPv4 settings of [connection] from its `connection show -f IPV4_FIELDS` answer. */
    fun ipv4(
        connection: String,
        lines: List<String>,
    ): Ipv4Settings {
        val fields = fields(lines)
        return Ipv4Settings(
            connection = connection,
            automatic = fields[METHOD] != METHOD_MANUAL,
            address = fields[ADDRESSES].orEmpty(),
            gateway = fields[GATEWAY].orEmpty(),
            dns = fields[DNS].orEmpty(),
            autoconnect = fields[AUTOCONNECT] != NO,
        )
    }

    /**
     * Arguments of `nmcli connection modify` for [settings]. Automatic clears the manual values,
     * so switching back to DHCP leaves no stale address behind.
     */
    fun modifyArgs(settings: Ipv4Settings): List<String> = buildList {
        add("connection")
        add("modify")
        add(settings.connection)
        if (settings.automatic) {
            addAll(listOf(METHOD, METHOD_AUTO, ADDRESSES, "", GATEWAY, "", DNS, ""))
        } else {
            addAll(listOf(METHOD, METHOD_MANUAL, ADDRESSES, settings.address.trim()))
            addAll(listOf(GATEWAY, settings.gateway.trim()))
            addAll(listOf(DNS, Ipv4Validator.dnsList(settings.dns).joinToString(Ipv4Validator.LIST_SEPARATOR)))
        }
        addAll(listOf(AUTOCONNECT, if (settings.autoconnect) YES else NO))
    }
}
