package org.psippr.desktop.data

/**
 * Checks manual IPv4 input before it is applied, so a typo never reaches the network
 * configuration. Each check returns null when fine, else the message to show.
 */
object Ipv4Validator {
    /** Separator of several DNS servers. */
    const val LIST_SEPARATOR = ","

    private const val OCTETS = 4
    private const val OCTET_MAX = 255
    private const val PREFIX_MIN = 1
    private const val PREFIX_MAX = 32
    private const val PREFIX_SEPARATOR = "/"
    private const val OCTET_SEPARATOR = "."

    const val BAD_ADDRESS = "The address must look like 192.168.1.20/24."
    const val BAD_GATEWAY = "The gateway must look like 192.168.1.1, or stay empty."
    const val BAD_DNS = "DNS servers must look like 1.1.1.1, 9.9.9.9, or stay empty."

    /** The first problem of [settings], null when they can be applied. */
    fun problem(settings: Ipv4Settings): String? = when {
        settings.automatic -> null
        !isAddressWithPrefix(settings.address) -> BAD_ADDRESS
        settings.gateway.isNotBlank() && !isAddress(settings.gateway.trim()) -> BAD_GATEWAY
        settings.dns.isNotBlank() && !dnsList(settings.dns).all(::isAddress) -> BAD_DNS
        else -> null
    }

    /** DNS servers of a comma separated [text], trimmed, without empty entries. */
    fun dnsList(text: String): List<String> = text.split(LIST_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }

    /** True for `a.b.c.d/nn` with a valid address and a prefix of 1..32. */
    fun isAddressWithPrefix(text: String): Boolean {
        val parts = text.trim().split(PREFIX_SEPARATOR)
        val prefix = parts.getOrNull(1)?.toIntOrNull() ?: return false
        return parts.size == 2 && isAddress(parts[0]) && prefix in PREFIX_MIN..PREFIX_MAX
    }

    /** True for a dotted IPv4 address `a.b.c.d`. */
    fun isAddress(text: String): Boolean {
        val octets = text.split(OCTET_SEPARATOR)
        return octets.size == OCTETS &&
            octets.all { octet ->
                octet.isNotEmpty() &&
                    octet.all(Char::isDigit) &&
                    (octet.toIntOrNull() ?: -1) in 0..OCTET_MAX
            }
    }
}
