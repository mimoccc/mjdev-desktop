package org.psippr.desktop.managers.devices

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression tests of the unknown device flow: a new device is asked about, shown only after
 * Connect, hidden after Dismiss, and with "Remember device" shown at once without asking again.
 */
class DeviceGateTest {
    private val phone = ConnectedDevice(id = "adb:R5CT1", provider = "adb", name = "Phone", state = DeviceState.READY)
    private val tablet = ConnectedDevice(id = "adb:ZY22", provider = "adb", name = "Tablet", state = DeviceState.READY)

    @Test
    fun unknownDeviceIsAskedOnceAndHiddenUntilConnected() {
        val gate = DeviceGate(emptyList<KnownDevice>())
        assertEquals(listOf(phone), gate.toAsk(listOf(phone)))
        assertTrue(gate.toAsk(listOf(phone)).isEmpty(), "asked only once")
        assertTrue(gate.visible(listOf(phone)).isEmpty(), "not shown before Connect")
    }

    @Test
    fun connectShowsTheDevice() {
        val gate = DeviceGate(emptyList<KnownDevice>())
        gate.toAsk(listOf(phone))
        gate.connect(phone.id, remember = false)
        assertEquals(listOf(phone), gate.visible(listOf(phone, tablet)))
        assertTrue(gate.rememberedIds().isEmpty(), "not remembered without the checkbox")
    }

    @Test
    fun dismissKeepsTheDeviceHiddenAndUnasked() {
        val gate = DeviceGate(emptyList<KnownDevice>())
        gate.toAsk(listOf(phone))
        gate.dismiss(phone.id)
        assertTrue(gate.visible(listOf(phone)).isEmpty())
        assertTrue(gate.toAsk(listOf(phone)).isEmpty())
    }

    @Test
    fun replugAsksAgainUnlessRemembered() {
        val gate = DeviceGate(emptyList<KnownDevice>())
        gate.toAsk(listOf(phone, tablet))
        gate.connect(phone.id, remember = false)
        gate.dismiss(tablet.id)
        gate.forgetGone(emptyList())
        assertEquals(listOf(phone, tablet), gate.toAsk(listOf(phone, tablet)))
    }

    /** Regression: a remembered device could not be removed; Forget flags it and asks again on replug. */
    @Test
    fun forgetHidesFlagsAndAsksAgainAfterReplug() {
        val gate = DeviceGate(emptyList<KnownDevice>())
        gate.toAsk(listOf(phone))
        gate.connect(phone, remember = true)
        gate.forget(phone.id)
        assertTrue(gate.visible(listOf(phone)).isEmpty(), "hidden after Forget")
        assertTrue(gate.toAsk(listOf(phone)).isEmpty(), "not asked again while still plugged in")
        assertTrue(gate.knownDevices().single().forgotten, "flagged, not deleted")
        gate.forgetGone(emptyList())
        assertEquals(listOf(phone), gate.toAsk(listOf(phone)), "asked again after replug")
    }

    /** Regression: the screen mirror showed a dismissed device; only Connect or Remember allow it. */
    @Test
    fun onlyConnectedOrRememberedDevicesAreAllowed() {
        val gate = DeviceGate(emptyList<KnownDevice>())
        gate.toAsk(listOf(phone, tablet))
        assertTrue(gate.allowedIds().isEmpty(), "nothing allowed before the answer")
        gate.dismiss(tablet.id)
        gate.connect(phone, remember = true)
        assertEquals(listOf(phone.id), gate.allowedIds())
        gate.forget(phone.id)
        assertTrue(gate.allowedIds().isEmpty(), "not allowed after Forget")
    }

    @Test
    fun unpluggedRememberedDeviceIsListedForForget() {
        val gate = DeviceGate(emptyList<KnownDevice>())
        gate.connect(phone, remember = true)
        assertEquals(listOf("Phone"), gate.rememberedAbsent(emptyList()).map { it.name })
        assertTrue(gate.rememberedAbsent(listOf(phone.id)).isEmpty())
    }

    @Test
    fun firstFormatIdsAreTakenOverAndGetTheirName() {
        val gate = DeviceGate(KnownDevices(remembered = mutableListOf(phone.id)).entries())
        assertEquals(listOf(phone), gate.visible(listOf(phone)))
        assertTrue(gate.updateNames(listOf(phone)))
        assertEquals("Phone", gate.knownDevices().single().name)
    }

    @Test
    fun rememberedDeviceIsShownWithoutAsking() {
        val gate = DeviceGate(emptyList<KnownDevice>())
        gate.toAsk(listOf(phone))
        gate.connect(phone.id, remember = true)
        val next = DeviceGate(gate.rememberedIds())
        assertTrue(next.toAsk(listOf(phone)).isEmpty())
        assertEquals(listOf(phone), next.visible(listOf(phone)))
    }
}
