package org.psippr.desktop.managers.display.mutter

/**
 * Reads the GVariant text `gdbus call … GetCurrentState` prints, of the form
 * `(uint32 serial, [monitors], [logical monitors], {properties})`. Only the parts needed to show
 * and switch the resolution are read.
 */
object MutterStateParser {
    // (uint32 1, …
    private val SERIAL = Regex("^\\(uint32 (\\d+),")

    // (('eDP-1', 'BOE', '0x085b', '0x00000000'), [ … the start of one monitor and its mode list
    private val MONITOR = Regex("\\(\\('([^']*)', '[^']*', '[^']*', '[^']*'\\), \\[")

    // ('1920x1080@60.000', 1920, 1080, 60.0, 1.0, [1.0, 1.25], {'is-current': <true>})
    private val MODE =
        Regex("\\('([^']+)', (\\d+), (\\d+), ([0-9.]+), [0-9.]+, \\[([^\\]]*)], \\{([^}]*)}\\)")

    // (0, 0, 1.0, uint32 0, true, [('eDP-1', 'BOE', '0x085b', '0x00000000')]
    private val LOGICAL =
        Regex(
            "\\((-?\\d+), (-?\\d+), ([0-9.]+), uint32 (\\d+), (true|false), " +
                "\\[((?:\\('[^']*', '[^']*', '[^']*', '[^']*'\\)(?:, )?)*)]",
        )

    // ('eDP-1', 'BOE', '0x085b', '0x00000000') inside a logical monitor: the connector
    private val CONNECTOR = Regex("\\('([^']*)', '[^']*', '[^']*', '[^']*'\\)")

    /** Flag a mode carries while the monitor runs in it. */
    private const val CURRENT_FLAG = "'is-current': <true>"

    /** Separator of the scale list. */
    private const val LIST_SEPARATOR = ","

    /** GVariant boolean true. */
    private const val TRUE = "true"

    /** The state in [text], null when it is not a GetCurrentState answer. */
    fun parse(text: String): MutterState? {
        val serial = SERIAL.find(text.trim())?.groupValues?.get(1)?.toLongOrNull() ?: return null
        val logical = LOGICAL.findAll(text).toList()
        val monitorsEnd = logical.firstOrNull()?.range?.first ?: text.length
        val headers = MONITOR.findAll(text).filter { header -> header.range.first < monitorsEnd }.toList()
        val monitors =
            headers.mapIndexed { index, header ->
                val end = headers.getOrNull(index + 1)?.range?.first ?: monitorsEnd
                MutterMonitor(
                    connector = header.groupValues[1],
                    modes = MODE.findAll(text.substring(header.range.last + 1, end)).map(::mode).toList(),
                )
            }
        return MutterState(serial, monitors, logical.map(::logicalMonitor))
    }

    private fun mode(match: MatchResult): MutterMode {
        val (id, width, height, refresh, scales, flags) = match.destructured
        return MutterMode(
            id = id,
            width = width.toInt(),
            height = height.toInt(),
            refreshHz = refresh.toDouble(),
            scales = scales.split(LIST_SEPARATOR).mapNotNull { scale -> scale.trim().toDoubleOrNull() },
            current = flags.contains(CURRENT_FLAG),
        )
    }

    private fun logicalMonitor(match: MatchResult): MutterLogicalMonitor {
        val (x, y, scale, transform, primary, connectors) = match.destructured
        return MutterLogicalMonitor(
            x = x.toInt(),
            y = y.toInt(),
            scale = scale.toDouble(),
            transform = transform.toInt(),
            primary = primary == TRUE,
            connectors = CONNECTOR.findAll(connectors).map { connector -> connector.groupValues[1] }.toList(),
        )
    }
}
