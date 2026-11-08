package org.psippr.desktop.managers.connectivity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BluetoothctlOutputTest {
    /** `bluetoothctl devices` after a scan, with the colored event lines bluetoothctl mixes in. */
    private val devices =
        listOf(
            "Device $HEADPHONES JBL Live 670NC",
            "Device $CLOCK Nixie Clock",
            "Device $BEACON ${BEACON.replace(':', '-')}",
            "\u001B[0;92mNEW\u001B[0m] Media /org/bluez/hci0 ",
            "[\u001B[0;92mNEW\u001B[0m] Device $OTHER Other",
            "\tSupportedUUIDs: 0000110a-0000-1000-8000-00805f9b34fb",
        )

    /** Regression: a device found by a scan (not paired) must be listed so it can be connected. */
    @Test
    fun scannedDeviceIsListedAsNew() {
        val clock = BluetoothctlOutput.devices(devices, emptySet(), setOf(HEADPHONES)).single { it.address == CLOCK }
        assertEquals("Nixie Clock", clock.name)
        assertFalse(clock.paired)
    }

    @Test
    fun namelessBeaconIsHiddenUnlessPaired() {
        assertTrue(BluetoothctlOutput.devices(devices, emptySet(), emptySet()).none { it.address == BEACON })
        assertTrue(BluetoothctlOutput.devices(devices, emptySet(), setOf(BEACON)).any { it.address == BEACON })
    }

    @Test
    fun eventLinesAreNotPartOfTheList() {
        assertTrue(BluetoothctlOutput.devices(devices, emptySet(), emptySet()).none { it.address == OTHER })
        assertEquals(setOf(HEADPHONES, CLOCK, BEACON), BluetoothctlOutput.addresses(devices))
    }

    @Test
    fun connectedAndPairedFlagsFollowTheirLists() {
        val headphones =
            BluetoothctlOutput
                .devices(devices, setOf(HEADPHONES), setOf(HEADPHONES))
                .single { it.address == HEADPHONES }
        assertTrue(headphones.connected)
        assertTrue(headphones.paired)
    }

    @Test
    fun poweredIsReadFromShow() {
        assertTrue(BluetoothctlOutput.isPowered(listOf("Controller $HEADPHONES (public)", "\tPowered: yes")))
        assertFalse(BluetoothctlOutput.isPowered(listOf("\tPowered: no")))
        assertFalse(BluetoothctlOutput.isPowered(emptyList()))
    }

    private companion object {
        /** Paired headphones. */
        const val HEADPHONES = "AA:BB:CC:DD:EE:01"

        /** Named device found by a scan. */
        const val CLOCK = "AA:BB:CC:DD:EE:02"

        /** Nameless beacon bluez names after its address. */
        const val BEACON = "40:CB:C0:B1:31:21"

        /** Device only mentioned in an event line. */
        const val OTHER = "AA:BB:CC:DD:EE:03"
    }
}
