package org.psippr.desktop.managers.display.mutter

import org.psippr.desktop.helpers.system.shell.Shell
import org.psippr.desktop.managers.display.DisplayMode
import org.psippr.desktop.managers.display.IDisplayBackend
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Screen mode of the main monitor under GNOME, through mutter's `org.gnome.Mutter.DisplayConfig`
 * D-Bus api (`gdbus`, part of GLib). [method] is how a switch is applied, see [MutterApplyConfig].
 */
class MutterDisplayBackend(
    private val method: Int = MutterApplyConfig.METHOD_PERSISTENT,
) : IDisplayBackend {
    override fun currentMode(): DisplayMode? = state()?.mainMonitor?.currentMode?.let(::toDisplayMode)

    override fun modes(): List<DisplayMode> = state()
        ?.mainMonitor
        ?.modes
        ?.map(::toDisplayMode)
        .orEmpty()

    override fun setMode(mode: DisplayMode): Result<Boolean> {
        val state = state() ?: return Result.success(false)
        val monitor = state.mainMonitor ?: return Result.success(false)
        val target = monitor.modes.firstOrNull { candidate -> matches(candidate, mode) } ?: return Result.success(false)
        val logical =
            MutterApplyConfig.logicalMonitors(state, monitor.connector, target) ?: return Result.success(false)
        return Shell
            .execute(
                CMD_TIMEOUT,
                CALL_TIMEOUT_S,
                *call(METHOD_APPLY),
                state.serial.toString(),
                method.toString(),
                logical,
                MutterApplyConfig.NO_PROPERTIES,
            ).map { process -> process.exitValue() == 0 }
    }

    private fun state(): MutterState? = MutterStateParser.parse(
        Shell.executeAndReadLines(CMD_TIMEOUT, CALL_TIMEOUT_S, *call(METHOD_STATE)).joinToString(" "),
    )

    private fun call(method: String): Array<String> = arrayOf(
        CMD_GDBUS,
        "call",
        "--session",
        "--dest",
        BUS_NAME,
        "--object-path",
        OBJECT_PATH,
        "--method",
        "$INTERFACE.$method",
    )

    private fun toDisplayMode(mode: MutterMode): DisplayMode =
        DisplayMode(mode.width, mode.height, (mode.refreshHz * MILLI).roundToInt())

    private fun matches(
        candidate: MutterMode,
        mode: DisplayMode,
    ): Boolean = candidate.width == mode.width &&
        candidate.height == mode.height &&
        abs((candidate.refreshHz * MILLI).roundToInt() - mode.refreshMilliHz) <= REFRESH_TOLERANCE_MHZ

    private companion object {
        /** coreutils command limiting a call, so a hanging session bus never blocks the page. */
        const val CMD_TIMEOUT = "timeout"
        const val CALL_TIMEOUT_S = "5"
        const val CMD_GDBUS = "gdbus"
        const val BUS_NAME = "org.gnome.Mutter.DisplayConfig"
        const val OBJECT_PATH = "/org/gnome/Mutter/DisplayConfig"
        const val INTERFACE = "org.gnome.Mutter.DisplayConfig"
        const val METHOD_STATE = "GetCurrentState"
        const val METHOD_APPLY = "ApplyMonitorsConfig"

        /** mHz in one Hz. */
        const val MILLI = 1000

        /** Refresh difference (mHz) still treated as the same mode. */
        const val REFRESH_TOLERANCE_MHZ = 1
    }
}
