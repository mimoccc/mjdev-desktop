package org.mjdev.desktop.plugins

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.DoubleExt.toMemorySizeReadable
import org.mjdev.desktop.helpers.system.meminfo.MemInfo
import org.mjdev.desktop.plugins.remote.RemoteAlign
import org.mjdev.desktop.plugins.remote.RemoteColorToken
import org.mjdev.desktop.plugins.remote.RemoteDocument
import org.mjdev.desktop.plugins.remote.RemoteOp
import org.mjdev.desktop.plugins.remote.RemoteOpType
import org.mjdev.desktop.plugins.remote.RemoteVariables

/**
 * Built-in memory usage widget (donut chart with used / free legend). It is a regular
 * [IDesktopPlugin]: the look is a [RemoteDocument] and only the numbers come from code, exactly
 * what an external plugin jar would provide.
 */
class MemoryWidgetPlugin : IDesktopPlugin {
    override val document: RemoteDocument =
        RemoteDocument(
            ops =
                listOf(
                    ring(alpha = RING_TRACK_ALPHA, sweepVar = ""),
                    ring(alpha = 1f, sweepVar = VAR_USED_RATIO),
                    text("{$VAR_USED_PERCENT}", RING_Y, RING_H, CENTER_TEXT_SIZE),
                    text(TITLE, TITLE_Y, LINE_H, LINE_TEXT_SIZE),
                    text("$USED_LABEL: {$VAR_USED}", USED_Y, LINE_H, LINE_TEXT_SIZE),
                    text("$FREE_LABEL: {$VAR_FREE}", FREE_Y, LINE_H, LINE_TEXT_SIZE),
                ),
        )

    override val refreshMs: Long = REFRESH_MS

    override fun variables(context: IDesktopContext): RemoteVariables {
        val mem = MemInfo(context)
        val ratio = if (mem.total > 0.0) (mem.used / mem.total).toFloat() else 0f
        return RemoteVariables(
            numbers = mapOf(VAR_USED_RATIO to ratio),
            texts =
                mapOf(
                    VAR_USED_PERCENT to "${(ratio * PERCENT).toInt()}%",
                    VAR_USED to mem.used.toMemorySizeReadable(),
                    VAR_FREE to mem.free.toMemorySizeReadable(),
                ),
        )
    }

    /** One ring of the donut; the color token is always ICONS, only [alpha] differs. */
    private fun ring(
        alpha: Float,
        sweepVar: String,
    ) = RemoteOp(
        type = RemoteOpType.ARC.name,
        x = RING_X,
        y = RING_Y,
        w = RING_W,
        h = RING_H,
        color = RemoteColorToken.ICONS.name,
        alpha = alpha,
        stroke = RING_STROKE,
        sweepVar = sweepVar,
    )

    private fun text(
        value: String,
        y: Float,
        h: Float,
        size: Float,
    ) = RemoteOp(
        type = RemoteOpType.TEXT.name,
        y = y,
        h = h,
        color = RemoteColorToken.ICONS.name,
        text = value,
        textSize = size,
        align = RemoteAlign.CENTER.name,
    )

    companion object {
        /** Stable id used to persist whether the widget is enabled. */
        const val ID = "builtin.memory"

        /** Label of the plugin in the control center and the widget title. */
        const val TITLE = "Memory"

        /** Description shown when the plugin row is expanded. */
        const val DESCRIPTION = "Shows used and free system memory as a donut chart on the desktop."

        /** Author shown in the plugin details. */
        const val AUTHOR = "mjdev"

        /** Version shown in the plugin details. */
        const val VERSION = "1.0"

        private const val USED_LABEL = "Used"
        private const val FREE_LABEL = "Free"
        private const val VAR_USED_RATIO = "usedRatio"
        private const val VAR_USED_PERCENT = "usedPercent"
        private const val VAR_USED = "used"
        private const val VAR_FREE = "free"
        private const val PERCENT = 100
        private const val REFRESH_MS = 10_000L

        // layout, normalized to the widget size
        private const val RING_X = 0.25f
        private const val RING_Y = 0.03f
        private const val RING_W = 0.5f
        private const val RING_H = 0.6f
        private const val RING_STROKE = 0.12f
        private const val RING_TRACK_ALPHA = 0.25f
        private const val TITLE_Y = 0.65f
        private const val USED_Y = 0.76f
        private const val FREE_Y = 0.87f
        private const val LINE_H = 0.1f
        private const val LINE_TEXT_SIZE = 0.07f
        private const val CENTER_TEXT_SIZE = 0.1f

        /** Metadata of the built-in plugin. */
        val METADATA =
            PluginMetadata(
                id = ID,
                name = TITLE,
                description = DESCRIPTION,
                version = VERSION,
                author = AUTHOR,
            )
    }
}
