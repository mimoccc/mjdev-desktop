package org.psippr.desktop.helpers.system.environment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Regression test: launched apps get a usable D-Bus session bus address, never an unexpanded one. */
class EnvironmentLinuxTest {
    @Test
    fun sessionBusAddressIsKept() {
        val env = EnvironmentLinux { mapOf(EnvironmentLinux.DBUS_SESSION_BUS_ADDRESS to SESSION_BUS) }.toMap()
        assertEquals(SESSION_BUS, env[EnvironmentLinux.DBUS_SESSION_BUS_ADDRESS])
    }

    @Test
    fun busAddressIsBuiltFromRuntimeDir() {
        val env = EnvironmentLinux { mapOf(EnvironmentLinux.XDG_RUNTIME_DIR to RUNTIME_DIR) }.toMap()
        assertEquals(RUNTIME_BUS, env[EnvironmentLinux.DBUS_SESSION_BUS_ADDRESS])
    }

    @Test
    fun noBusAddressWithoutRuntimeDir() {
        val env = EnvironmentLinux { emptyMap() }.toMap()
        assertNull(env[EnvironmentLinux.DBUS_SESSION_BUS_ADDRESS])
    }

    private companion object {
        /** Bus address a session already has. */
        const val SESSION_BUS = "unix:path=/run/user/1000/bus,guid=abc"

        /** Runtime dir of a test user. */
        const val RUNTIME_DIR = "/run/user/1000"

        /** Bus address expected from [RUNTIME_DIR]. */
        const val RUNTIME_BUS = "unix:path=/run/user/1000/bus"
    }
}
