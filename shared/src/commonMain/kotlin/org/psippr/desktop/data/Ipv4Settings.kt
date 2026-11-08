package org.psippr.desktop.data

/**
 * IPv4 configuration of the connection a network device uses.
 *
 * @property connection name of the connection profile it belongs to
 * @property automatic true for DHCP; the manual fields are ignored then
 * @property address manual address with prefix, e.g. `192.168.1.20/24`
 * @property gateway manual gateway, empty for none
 * @property dns manual DNS servers, comma separated, empty for none
 * @property autoconnect true when the connection comes up by itself
 */
data class Ipv4Settings(
    val connection: String,
    val automatic: Boolean = true,
    val address: String = "",
    val gateway: String = "",
    val dns: String = "",
    val autoconnect: Boolean = true,
)
