package org.psippr.desktop.managers.display.mutter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MutterDisplayConfigTest {
    /** GetCurrentState of a laptop screen as gdbus prints it (mode list shortened). */
    private val laptop =
        "(uint32 1, [(('eDP-1', 'BOE', '0x085b', '0x00000000'), [" +
            "('1920x1080@60.000', 1920, 1080, 60.0, 1.0, [1.0, 1.25, 2.0], " +
            "{'is-current': <true>, 'is-preferred': <true>}), " +
            "('1920x1080@59.934', 1920, 1080, 59.933879852294922, 1.0, [1.0, 1.25, 2.0], {}), " +
            "('800x600@59.861', 800, 600, 59.861404418945312, 1.0, [1.0], {})], " +
            "{'is-builtin': <true>, 'display-name': <'Vestavěný displej'>, 'color-mode': <uint32 0>})], " +
            "[(0, 0, 2.0, uint32 0, true, [('eDP-1', 'BOE', '0x085b', '0x00000000')], @a{sv} {})], " +
            "{'layout-mode': <uint32 1>, 'supports-changing-layout-mode': <true>})"

    /** Two monitors side by side, the external one primary. */
    private val twoScreens =
        "(uint32 7, [(('eDP-1', 'BOE', 'a', 'b'), [" +
            "('1920x1080@60.000', 1920, 1080, 60.0, 1.0, [1.0], {'is-current': <true>})], {}), " +
            "(('HDMI-1', 'DEL', 'c', 'd'), [" +
            "('2560x1440@59.951', 2560, 1440, 59.951, 1.0, [1.0], {'is-current': <true>}), " +
            "('1920x1080@60.000', 1920, 1080, 60.0, 1.0, [1.0], {})], {})], " +
            "[(0, 0, 1.0, uint32 0, false, [('eDP-1', 'BOE', 'a', 'b')], @a{sv} {}), " +
            "(1920, 0, 1.0, uint32 0, true, [('HDMI-1', 'DEL', 'c', 'd')], @a{sv} {})], {})"

    @Test
    fun laptopStateIsRead() {
        val state = assertNotNull(MutterStateParser.parse(laptop))
        assertEquals(1L, state.serial)
        val monitor = assertNotNull(state.mainMonitor)
        assertEquals("eDP-1", monitor.connector)
        assertEquals(3, monitor.modes.size)
        assertEquals("1920x1080@60.000", monitor.currentMode?.id)
        assertEquals(listOf(1.0, 1.25, 2.0), monitor.modes.first().scales)
    }

    @Test
    fun primaryMonitorIsTheMainOne() {
        val state = assertNotNull(MutterStateParser.parse(twoScreens))
        assertEquals("HDMI-1", state.mainMonitor?.connector)
        assertEquals(2, state.logicalMonitors.size)
    }

    /** Regression: switching keeps the layout and puts the new mode id on the main monitor only. */
    @Test
    fun applyArgumentsSwitchOnlyTheMainMonitor() {
        val state = assertNotNull(MutterStateParser.parse(twoScreens))
        val target = state.monitors.single { it.connector == "HDMI-1" }.modes.single { it.width == 1920 }
        assertEquals(
            "[(0, 0, 1.0, uint32 0, false, [('eDP-1', '1920x1080@60.000', @a{sv} {})]), " +
                "(1920, 0, 1.0, uint32 0, true, [('HDMI-1', '1920x1080@60.000', @a{sv} {})])]",
            MutterApplyConfig.logicalMonitors(state, "HDMI-1", target),
        )
    }

    @Test
    fun scaleTheNewModeDoesNotAllowFallsBackToOne() {
        val state = assertNotNull(MutterStateParser.parse(laptop))
        val small = state.monitors.single().modes.single { it.width == 800 }
        val logical = assertNotNull(MutterApplyConfig.logicalMonitors(state, "eDP-1", small))
        assertTrue(logical.startsWith("[(0, 0, 1.0, uint32 0, true,"), logical)
    }

    @Test
    fun unknownConnectorGivesNoArguments() {
        val state = assertNotNull(MutterStateParser.parse(laptop))
        assertNull(MutterApplyConfig.logicalMonitors(state, "DP-9", state.monitors.single().modes.first()))
    }

    @Test
    fun otherTextIsNoState() {
        assertNull(MutterStateParser.parse("Error: GDBus.Error:org.freedesktop.DBus.Error.ServiceUnknown"))
    }
}
