package org.psippr.desktop.managers.devices.adb

import org.psippr.desktop.managers.devices.DeviceAction
import org.psippr.desktop.managers.devices.DeviceState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Regression tests: phones were shown as network interfaces; now adb's own answers are read. */
class AdbOutputTest {
    private val devicesAnswer =
        listOf(
            "* daemon started successfully",
            "List of devices attached",
            "R5CT1234567            device usb:1-2 product:dm1qxeea model:SM_S911B device:dm1q transport_id:3",
            "ZY22ABCDEF             unauthorized usb:1-3 transport_id:4",
            "192.168.1.40:5555      device product:sunfish model:Pixel_4a device:sunfish transport_id:5",
            "adb-1A2B3C-xyz._adb-tls-connect._tcp offline transport_id:6",
            "",
        )

    @Test
    fun deviceListIsRead() {
        val devices = AdbOutput.devices(devicesAnswer)
        assertEquals(
            listOf(
                "R5CT1234567",
                "ZY22ABCDEF",
                "192.168.1.40:5555",
                "adb-1A2B3C-xyz._adb-tls-connect._tcp",
            ),
            devices.map {
                it.serial
            },
        )
        assertEquals(
            listOf(
                DeviceState.READY,
                DeviceState.UNAUTHORIZED,
                DeviceState.READY,
                DeviceState.OFFLINE,
            ),
            devices.map {
                it.state
            },
        )
        assertEquals("SM S911B", devices[0].model)
        assertEquals(listOf(false, false, true, true), devices.map { it.wireless })
    }

    @Test
    fun infoAnswerIsRead() {
        val info =
            AdbOutput.info(
                listOf(
                    "samsung",
                    "SM-S911B",
                    "14",
                    "Current Battery Service state:",
                    "  AC powered: false",
                    "  USB powered: true",
                    "  level: 87",
                ),
            )
        assertEquals(AdbDeviceInfo("samsung", "SM-S911B", "14", 87, charging = true), info)
    }

    @Test
    fun missingBatteryIsUnknown() {
        val info = AdbOutput.info(listOf("Google", "Pixel 4a", "13"))
        assertNull(info.batteryPercent)
        assertFalse(info.charging)
    }

    @Test
    fun wifiAddressIsRead() {
        val answer =
            listOf(
                "30: wlan0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP group default qlen 3000",
                "    inet 192.168.1.40/24 brd 192.168.1.255 scope global wlan0",
            )
        assertEquals("192.168.1.40", AdbOutput.address(answer))
        assertNull(AdbOutput.address(listOf("Device \"wlan0\" does not exist.")))
    }

    @Test
    fun actionsFollowStateAndConnection() {
        val usb = AdbDeviceEntry("R5CT1234567", DeviceState.READY, "SM S911B", wireless = false)
        assertTrue(DeviceAction.ENABLE_WIRELESS in AdbActions.available(usb))
        assertFalse(DeviceAction.DISCONNECT in AdbActions.available(usb))
        val wireless = usb.copy(serial = "192.168.1.40:5555", wireless = true)
        assertTrue(DeviceAction.DISCONNECT in AdbActions.available(wireless))
        assertTrue(AdbActions.available(usb.copy(state = DeviceState.UNAUTHORIZED)).isEmpty())
        assertEquals(listOf(DeviceAction.DISCONNECT), AdbActions.available(wireless.copy(state = DeviceState.OFFLINE)))
    }
}
