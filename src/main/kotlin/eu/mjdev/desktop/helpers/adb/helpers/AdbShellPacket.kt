package eu.mjdev.desktop.helpers.adb.helpers

import eu.mjdev.desktop.helpers.adb.helpers.AdbShellStream.Companion.ID_EXIT
import eu.mjdev.desktop.helpers.adb.helpers.AdbShellStream.Companion.ID_STDERR
import eu.mjdev.desktop.helpers.adb.helpers.AdbShellStream.Companion.ID_STDOUT

sealed class AdbShellPacket(
    open val payload: ByteArray
) {
    abstract val id: Int

    class StdOut(override val payload: ByteArray) : AdbShellPacket(payload) {
        override val id: Int = ID_STDOUT
        override fun toString() = "STDOUT: ${String(payload)}"
    }

    class StdError(override val payload: ByteArray) : AdbShellPacket(payload) {
        override val id: Int = ID_STDERR
        override fun toString() = "STDERR: ${String(payload)}"
    }

    class Exit(override val payload: ByteArray) : AdbShellPacket(payload) {
        override val id: Int = ID_EXIT
        override fun toString() = "EXIT: ${payload[0]}"
    }
}