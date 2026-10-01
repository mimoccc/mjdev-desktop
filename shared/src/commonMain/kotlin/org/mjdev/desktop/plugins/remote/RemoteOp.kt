package org.mjdev.desktop.plugins.remote

/**
 * One drawing instruction of a [RemoteDocument]. All geometry is normalized (0..1) to the widget
 * size so the same document renders at any size. Every field has a default for Gson.
 */
data class RemoteOp(
    /** Operation name, see [RemoteOpType]. */
    val type: String = "",
    /** Left edge of the bounds, 0..1 of the widget width. */
    val x: Float = 0f,
    /** Top edge of the bounds, 0..1 of the widget height. */
    val y: Float = 0f,
    /** Bounds width, 0..1 of the widget width. */
    val w: Float = 1f,
    /** Bounds height, 0..1 of the widget height. */
    val h: Float = 1f,
    /** Color: a [RemoteColorToken] name, `#RRGGBB` or `#AARRGGBB`. */
    val color: String = "",
    /** Alpha multiplier applied to [color], 0..1. */
    val alpha: Float = 1f,
    /** Stroke width, 0..1 of the smaller bounds side. Zero draws a filled shape. */
    val stroke: Float = 0f,
    /** ARC: angle where the arc starts, degrees, -90 is the top. */
    val startAngle: Float = DEFAULT_START_ANGLE,
    /** ARC: static sweep in degrees, used when [sweepVar] is empty. */
    val sweep: Float = FULL_CIRCLE,
    /** ARC: name of a [RemoteVariables.numbers] entry (0..1) that scales the sweep. */
    val sweepVar: String = "",
    /** TEXT: the text, `{name}` placeholders come from [RemoteVariables.texts]. */
    val text: String = "",
    /** TEXT: font size, 0..1 of the widget height. */
    val textSize: Float = DEFAULT_TEXT_SIZE,
    /** TEXT: horizontal placement inside the bounds, see [RemoteAlign]. */
    val align: String = RemoteAlign.CENTER.name,
) {
    companion object {
        /** Start angle that points the arc to the top of the widget. */
        const val DEFAULT_START_ANGLE = -90f

        /** Degrees of a full circle. */
        const val FULL_CIRCLE = 360f

        /** Default text size relative to the widget height. */
        const val DEFAULT_TEXT_SIZE = 0.07f
    }
}
