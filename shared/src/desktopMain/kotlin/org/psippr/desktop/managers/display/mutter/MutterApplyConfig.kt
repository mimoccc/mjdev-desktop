package org.psippr.desktop.managers.display.mutter

import java.util.Locale

/**
 * Builds the arguments of mutter's `ApplyMonitorsConfig` for `gdbus call`: the current layout
 * unchanged, except that one monitor gets a new mode.
 */
object MutterApplyConfig {
    /** `ApplyMonitorsConfig` method: only check the configuration, change nothing. */
    const val METHOD_VERIFY = 0

    /** `ApplyMonitorsConfig` method: apply and keep it after a restart (monitors.xml). */
    const val METHOD_PERSISTENT = 2

    /** Empty GVariant dictionary (no extra properties). */
    const val NO_PROPERTIES = "@a{sv} {}"

    /** Scale every mode supports, used when the current scale does not fit the new mode. */
    private const val FALLBACK_SCALE = 1.0

    /** Allowed rounding difference when comparing scales. */
    private const val SCALE_TOLERANCE = 0.001

    /**
     * Logical monitors argument (`a(iiduba(ssa{sv}))`) of [state] with [connector] switched to
     * [mode]; null when the connector is not part of any logical monitor or a monitor is off.
     */
    fun logicalMonitors(
        state: MutterState,
        connector: String,
        mode: MutterMode,
    ): String? {
        if (state.logicalMonitors.none { area -> connector in area.connectors }) return null
        val areas =
            state.logicalMonitors.map { area ->
                val monitors =
                    area.connectors.map { name ->
                        val modeId =
                            if (name == connector) {
                                mode.id
                            } else {
                                state.monitors.firstOrNull { it.connector == name }?.currentMode?.id ?: return null
                            }
                        "('$name', '$modeId', $NO_PROPERTIES)"
                    }
                val scale = if (connector in area.connectors) scaleFor(area.scale, mode) else area.scale
                "(${area.x}, ${area.y}, ${number(scale)}, uint32 ${area.transform}, ${area.primary}, " +
                    "[${monitors.joinToString(", ")}])"
            }
        return "[${areas.joinToString(", ")}]"
    }

    // the new mode may not allow the current scale (e.g. 2.0 on 800x600): fall back to 1.0
    private fun scaleFor(
        current: Double,
        mode: MutterMode,
    ): Double = if (mode.scales.isEmpty() || mode.scales.any { kotlin.math.abs(it - current) < SCALE_TOLERANCE }) {
        current
    } else {
        FALLBACK_SCALE
    }

    // GVariant doubles need a dot whatever the locale
    private fun number(value: Double): String = String.format(Locale.ROOT, "%s", value)
}
