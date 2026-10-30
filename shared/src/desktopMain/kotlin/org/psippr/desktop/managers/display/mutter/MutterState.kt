package org.psippr.desktop.managers.display.mutter

/**
 * mutter's display configuration as `GetCurrentState` reports it.
 *
 * @property serial configuration serial, required by `ApplyMonitorsConfig`
 * @property monitors physical monitors
 * @property logicalMonitors screen areas and which monitors show them
 */
data class MutterState(
    val serial: Long,
    val monitors: List<MutterMonitor>,
    val logicalMonitors: List<MutterLogicalMonitor>,
) {
    /** Monitor of the primary screen area (the first monitor when none is marked primary). */
    val mainMonitor: MutterMonitor?
        get() {
            val connector =
                (logicalMonitors.firstOrNull { area -> area.primary } ?: logicalMonitors.firstOrNull())
                    ?.connectors
                    ?.firstOrNull()
            return monitors.firstOrNull { monitor -> monitor.connector == connector } ?: monitors.firstOrNull()
        }
}
