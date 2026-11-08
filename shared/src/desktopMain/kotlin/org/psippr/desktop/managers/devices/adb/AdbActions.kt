package org.psippr.desktop.managers.devices.adb

import org.psippr.desktop.helpers.files.FileChooser
import org.psippr.desktop.managers.devices.DeviceAction
import org.psippr.desktop.managers.devices.DeviceActionResult
import org.psippr.desktop.managers.devices.DeviceState
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** The [DeviceAction]s of an ADB device, run with the adb command. */
class AdbActions(
    private val picturesDir: File,
) {
    /** Runs [action] on the device [serial] named [name] (for messages and file names). */
    fun perform(
        serial: String,
        name: String,
        action: DeviceAction,
    ): DeviceActionResult = when (action) {
        DeviceAction.SEND_FILES -> sendFiles(serial)
        DeviceAction.INSTALL_APP -> installApp(serial)
        DeviceAction.SCREENSHOT -> screenshot(serial, name)
        DeviceAction.OPEN_SHELL -> openShell(serial)
        DeviceAction.ENABLE_WIRELESS -> enableWireless(serial)
        DeviceAction.DISCONNECT -> result(AdbCli.run(null, "disconnect", serial), "Disconnected.")
        DeviceAction.RECONNECT -> reconnect(serial)
        // handled by the devices manager for every provider; nothing to do on the device
        DeviceAction.FORGET -> DeviceActionResult(done = true)
        DeviceAction.REBOOT -> result(AdbCli.run(serial, "reboot"), "Restarting.")
        DeviceAction.REBOOT_RECOVERY -> result(AdbCli.run(serial, "reboot", "recovery"), "Restarting into recovery.")
        DeviceAction.REBOOT_BOOTLOADER ->
            result(
                AdbCli.run(serial, "reboot", "bootloader"),
                "Restarting into bootloader.",
            )
    }

    private fun sendFiles(serial: String): DeviceActionResult {
        val files = FileChooser.open(TITLE_SEND, multiple = true)
        if (files.isEmpty()) return DeviceActionResult.CANCELLED
        val answer =
            AdbCli.run(
                serial,
                "push",
                *files.map { it.path }.toTypedArray(),
                DOWNLOAD_DIR,
                timeoutS = AdbCli.TRANSFER_TIMEOUT_S,
            )
        return result(answer, "Sent ${files.size} file(s) to $DOWNLOAD_DIR.")
    }

    private fun installApp(serial: String): DeviceActionResult {
        val apk =
            FileChooser.open(TITLE_INSTALL, setOf(APK_EXTENSION)).firstOrNull() ?: return DeviceActionResult.CANCELLED
        // -r: replace an installed version (an update keeps the app's data)
        val answer = AdbCli.run(serial, "install", "-r", apk.path, timeoutS = AdbCli.TRANSFER_TIMEOUT_S)
        return result(answer, "Installed ${apk.name}.")
    }

    private fun screenshot(
        serial: String,
        name: String,
    ): DeviceActionResult {
        val png =
            AdbCli.bytes(serial, "exec-out", "screencap", "-p")
                ?: return DeviceActionResult(done = false, message = "The device sent no screenshot.")
        picturesDir.mkdirs()
        val file = File(picturesDir, "${fileName(name)}-${LocalDateTime.now().format(STAMP)}.$PNG_EXTENSION")
        file.writeBytes(png)
        return DeviceActionResult(done = true, message = "Saved ${file.path}.")
    }

    // a terminal of the system with the device's shell in it
    private fun openShell(serial: String): DeviceActionResult {
        val terminal =
            TERMINALS.firstOrNull { (command, _) -> onPath(command) }
                ?: return DeviceActionResult(done = false, message = "No terminal program found.")
        ProcessBuilder(
            listOf(terminal.first) + terminal.second + listOf(AdbCli.ADB, AdbCli.SERIAL_OPTION, serial, "shell"),
        ).inheritIO()
            .start()
        return DeviceActionResult(done = true)
    }

    // tcpip mode on the device, then connect to its Wi-Fi address; the cable can go afterwards
    private fun enableWireless(serial: String): DeviceActionResult {
        val address =
            AdbOutput.address(AdbCli.run(serial, "shell", "ip", "-f", "inet", "addr", "show", WIFI_INTERFACE).lines)
                ?: return DeviceActionResult(done = false, message = "The device is not on Wi-Fi.")
        if (!AdbCli.run(serial, "tcpip", WIRELESS_PORT).ok) {
            return DeviceActionResult(done = false, message = "The device did not switch to wireless.")
        }
        Thread.sleep(TCPIP_RESTART_MS)
        val target = "$address:$WIRELESS_PORT"
        val answer = AdbCli.run(null, "connect", target)
        val connected = answer.ok && answer.lines.any { it.contains(CONNECTED_MARK) }
        return if (connected) {
            DeviceActionResult(done = true, message = "Connected over Wi-Fi as $target; the cable can be unplugged.")
        } else {
            DeviceActionResult(
                done = false,
                message =
                    answer.lines.lastOrNull { it.isNotBlank() } ?: "Could not connect to $target.",
            )
        }
    }

    // adb connect to the remembered network address; success only when adb reports it connected
    private fun reconnect(serial: String): DeviceActionResult {
        val answer = AdbCli.run(null, "connect", serial)
        return if (answer.ok && answer.lines.any { it.contains(CONNECTED_MARK) }) {
            DeviceActionResult(done = true, message = "Connected over the network.")
        } else {
            DeviceActionResult(
                done = false,
                message =
                    answer.lines.lastOrNull { it.isNotBlank() } ?: "Could not connect to $serial.",
            )
        }
    }

    private fun result(
        answer: AdbCli.Answer,
        success: String,
    ): DeviceActionResult = if (answer.ok) {
        DeviceActionResult(done = true, message = success)
    } else {
        DeviceActionResult(done = false, message = answer.lines.lastOrNull { it.isNotBlank() } ?: "adb failed.")
    }

    private fun onPath(command: String): Boolean = System
        .getenv(PATH_ENV)
        .orEmpty()
        .split(File.pathSeparator)
        .any { dir -> File(dir, command).canExecute() }

    private fun fileName(name: String): String = name.replace(Regex("[^A-Za-z0-9._-]+"), "-").trim('-').ifBlank {
        SCREENSHOT_NAME
    }

    companion object {
        /** Actions a device in this state and connection offers. */
        fun available(entry: AdbDeviceEntry): List<DeviceAction> = when {
            entry.state == DeviceState.READY ->
                buildList {
                    add(DeviceAction.SEND_FILES)
                    add(DeviceAction.INSTALL_APP)
                    add(DeviceAction.SCREENSHOT)
                    add(DeviceAction.OPEN_SHELL)
                    add(if (entry.wireless) DeviceAction.DISCONNECT else DeviceAction.ENABLE_WIRELESS)
                    add(DeviceAction.REBOOT)
                    add(DeviceAction.REBOOT_RECOVERY)
                    add(DeviceAction.REBOOT_BOOTLOADER)
                }
            entry.wireless -> listOf(DeviceAction.DISCONNECT)
            else -> emptyList()
        }

        /** Folder on the device receiving sent files. */
        const val DOWNLOAD_DIR = "/sdcard/Download/"

        /** Port of wireless (tcpip) adb. */
        const val WIRELESS_PORT = "5555"

        /** Network interface of the device's Wi-Fi. */
        private const val WIFI_INTERFACE = "wlan0"

        /** Time the device's adb daemon needs to restart in tcpip mode. */
        private const val TCPIP_RESTART_MS = 2_000L

        /** Text of `adb connect` on success. */
        private const val CONNECTED_MARK = "connected to"
        private const val TITLE_SEND = "Send files to the device"
        private const val TITLE_INSTALL = "Install an app on the device"
        private const val APK_EXTENSION = "apk"
        private const val PNG_EXTENSION = "png"
        private const val SCREENSHOT_NAME = "device"
        private const val PATH_ENV = "PATH"
        private val STAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

        /**
         * Terminal programs tried in order, with the option that runs a command in them. Debian's
         * x-terminal-emulator wrapper comes last: its -e takes one word only on some terminals.
         */
        private val TERMINALS =
            listOf(
                "gnome-terminal" to listOf("--"),
                "konsole" to listOf("-e"),
                "xterm" to listOf("-e"),
                "x-terminal-emulator" to listOf("-e"),
            )
    }
}
