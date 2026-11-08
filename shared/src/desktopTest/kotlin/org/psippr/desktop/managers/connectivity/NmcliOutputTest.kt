package org.psippr.desktop.managers.connectivity

import org.psippr.desktop.data.Ipv4Settings
import org.psippr.desktop.data.Ipv4Validator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NmcliOutputTest {
    /**
     * Regression: wired adapters were looked up by a sysfs "ethernet" folder that does not exist,
     * so the Ethernet tab never showed. NetworkManager's device list is read instead.
     */
    @Test
    fun wiredAdaptersComeFromNetworkManager() {
        val lines =
            listOf(
                "wlp0s20f3:wifi:connected",
                "enp3s0:ethernet:connected",
                "veth1a2b:ethernet:unmanaged",
                "psv1:ethernet:connecting (getting IP configuration)",
                "lo:loopback:connected (externally)",
            )
        assertEquals(listOf("enp3s0", "psv1"), NmcliOutput.ethernetDevices(lines))
    }

    @Test
    fun manualSettingsAreRead() {
        val lines =
            listOf(
                "ipv4.method:manual",
                "ipv4.addresses:10.99.0.2/24",
                "ipv4.gateway:",
                "ipv4.dns:",
                "connection.autoconnect:no",
            )
        assertEquals(
            Ipv4Settings("psippr-test", automatic = false, address = "10.99.0.2/24", autoconnect = false),
            NmcliOutput.ipv4("psippr-test", lines),
        )
    }

    @Test
    fun automaticSettingsAreRead() {
        val lines = listOf("ipv4.method:auto", "ipv4.addresses:--", "connection.autoconnect:yes")
        val settings = NmcliOutput.ipv4("Wired connection 1", lines)
        assertTrue(settings.automatic)
        assertTrue(settings.autoconnect)
        assertEquals("", settings.address)
    }

    @Test
    fun manualArgumentsCarryEveryValue() {
        val settings =
            Ipv4Settings("Wired connection 1", false, "192.168.1.20/24", "192.168.1.1", "1.1.1.1 , 9.9.9.9", true)
        assertEquals(
            listOf(
                "connection",
                "modify",
                "Wired connection 1",
                "ipv4.method",
                "manual",
                "ipv4.addresses",
                "192.168.1.20/24",
                "ipv4.gateway",
                "192.168.1.1",
                "ipv4.dns",
                "1.1.1.1,9.9.9.9",
                "connection.autoconnect",
                "yes",
            ),
            NmcliOutput.modifyArgs(settings),
        )
    }

    /** Back to DHCP clears the manual values, so no stale address is left in the profile. */
    @Test
    fun automaticArgumentsClearManualValues() {
        val args =
            NmcliOutput.modifyArgs(
                Ipv4Settings("c", automatic = true, address = "10.0.0.2/24", autoconnect = false),
            )
        assertEquals(listOf("ipv4.method", "auto", "ipv4.addresses", ""), args.subList(3, 7))
        assertEquals(listOf("connection.autoconnect", "no"), args.takeLast(2))
    }

    @Test
    fun validatorRefusesTyposAndAcceptsGoodInput() {
        val good = Ipv4Settings("c", false, "192.168.1.20/24", "192.168.1.1", "1.1.1.1, 9.9.9.9")
        assertNull(Ipv4Validator.problem(good))
        assertEquals(Ipv4Validator.BAD_ADDRESS, Ipv4Validator.problem(good.copy(address = "192.168.1.20")))
        assertEquals(Ipv4Validator.BAD_ADDRESS, Ipv4Validator.problem(good.copy(address = "192.168.1.300/24")))
        assertEquals(Ipv4Validator.BAD_ADDRESS, Ipv4Validator.problem(good.copy(address = "192.168.1.20/33")))
        assertEquals(Ipv4Validator.BAD_GATEWAY, Ipv4Validator.problem(good.copy(gateway = "router")))
        assertEquals(Ipv4Validator.BAD_DNS, Ipv4Validator.problem(good.copy(dns = "1.1.1.1, x")))
        assertNull(Ipv4Validator.problem(good.copy(gateway = "", dns = "")))
        assertNull(Ipv4Validator.problem(good.copy(automatic = true, address = "junk")))
        assertFalse(Ipv4Validator.isAddress("1.2.3"))
    }
}
