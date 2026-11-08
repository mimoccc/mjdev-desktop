package org.psippr.desktop.managers.connectivity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression test: power on must survive bluez answering Busy while it enables the adapter. */
class BluetoothPowerRetryTest {
    @Test
    fun busyAnswersAreRetriedUntilSuccess() {
        var calls = 0
        val result =
            ConnectivityManagerLinux.retrying(ATTEMPTS, NO_DELAY) {
                calls++
                Result.success(calls == SUCCESS_ON_CALL)
            }
        assertTrue(result.getOrDefault(false))
        assertEquals(SUCCESS_ON_CALL, calls)
    }

    @Test
    fun retriesStopAfterLimit() {
        var calls = 0
        val result =
            ConnectivityManagerLinux.retrying(ATTEMPTS, NO_DELAY) {
                calls++
                Result.failure(IllegalStateException(BUSY))
            }
        assertFalse(result.getOrDefault(false))
        assertEquals(ATTEMPTS, calls)
    }

    private companion object {
        const val ATTEMPTS = 5
        const val NO_DELAY = 0L
        const val SUCCESS_ON_CALL = 3
        const val BUSY = "org.bluez.Error.Busy"
    }
}
