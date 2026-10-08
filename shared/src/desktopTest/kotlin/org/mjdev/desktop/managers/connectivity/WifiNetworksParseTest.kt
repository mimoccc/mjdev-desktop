package org.mjdev.desktop.managers.connectivity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Regression test: an SSID served by several access points must keep the connected one. */
class WifiNetworksParseTest {
    /** Real terse `nmcli -t -f ALL dev wifi` output (czech locale), the third AP is the active one. */
    private val lines = listOf(
        "AP[1]:Home:486F6D65:3C\\:64\\:CF\\:59\\:F9\\:24:Infra:2:2417 MHz:270 Mbit/s:40 MHz:82:▂▄▆█:WPA2:(nic):" +
            "pair_ccmp group_ccmp psk:wlp0s20f3:ne: :/org/freedesktop/NetworkManager/AccessPoint/372",
        "AP[2]:Home:486F6D65:3C\\:64\\:CF\\:59\\:F9\\:25:Infra:36:5180 MHz:135 Mbit/s:80 MHz:55:▂▄__:WPA2:(nic):" +
            "pair_ccmp group_ccmp psk:wlp0s20f3:ne: :/org/freedesktop/NetworkManager/AccessPoint/339",
        "AP[3]:Home:486F6D65:F0\\:4B\\:8A\\:F2\\:C4\\:F5:Infra:36:5180 MHz:540 Mbit/s:80 MHz:48:▂▄__:WPA2:(nic):" +
            "pair_ccmp group_ccmp psk:wlp0s20f3:ano:*:/org/freedesktop/NetworkManager/AccessPoint/371",
    )

    @Test
    fun connectedAccessPointIsKept() {
        val networks = ConnectivityManagerLinux.parseWifiNetworks(lines)
        assertEquals(1, networks.size)
        assertTrue(networks.single().isActive)
        assertEquals("AP[3]", networks.single().name)
    }
}
