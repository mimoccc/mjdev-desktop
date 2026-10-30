package org.psippr.desktop.components.controlcenter.pages.ethernet

/** Labels of the Ethernet tab's IPv4 settings. */
object EthPageDefaults {
    /** Title of the IPv4 section; the adapter name follows. */
    const val IPV4_TITLE = "IPv4 · "

    const val METHOD = "Method"
    const val AUTOMATIC = "Automatic (DHCP)"
    const val MANUAL = "Manual"
    const val ADDRESS = "Address / prefix"
    const val ADDRESS_HINT = "192.168.1.20/24"
    const val GATEWAY = "Gateway"
    const val GATEWAY_HINT = "192.168.1.1"
    const val DNS = "DNS servers"
    const val DNS_HINT = "1.1.1.1, 9.9.9.9"
    const val AUTOCONNECT = "Connect automatically"
    const val APPLY_LABEL = "Use these settings"
    const val APPLY = "Apply"
    const val APPLYING = "Applying…"
    const val APPLIED = "IPv4 settings applied."
    const val REFUSED = "NetworkManager did not accept the settings."
}
