package org.mjdev.desktop.data

/** Network device details the control center shows, with the system field they come from. */
enum class NetDetail(
    /** Field name as reported by the connectivity manager. */
    val key: String,
    /** Label shown to the user. */
    val label: String,
) {
    State("GENERAL.STATE", "State"),
    Connection("GENERAL.CONNECTION", "Connection"),
    Address("IP4.ADDRESS", "IP address"),
    Gateway("IP4.GATEWAY", "Gateway"),
    Dns("IP4.DNS", "DNS"),
    Mac("GENERAL.HWADDR", "MAC address"),
    ;

    companion object {
        /** Marker in the state text of a device that has an active connection. */
        private const val CONNECTED_MARK = "(connected)"

        /** True when [details] describe a device with an active connection. */
        fun isConnected(details: Map<String, String>): Boolean =
            details[State.key].orEmpty().contains(CONNECTED_MARK)
    }
}
