package org.mjdev.desktop.plugins.remote

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import org.mjdev.desktop.managers.palette.IPalette

/** Paints single [RemoteOp]s onto a [DrawScope]; stateless so it is cheap to reuse every frame. */
object RemoteOpPainter {
    /** Placeholder delimiters in text ops. */
    private const val PLACEHOLDER_START = "{"
    private const val PLACEHOLDER_END = "}"

    /** Paints every op of [document] in order. */
    fun DrawScope.paint(
        document: RemoteDocument,
        variables: RemoteVariables,
        palette: IPalette,
        textMeasurer: TextMeasurer,
    ) {
        document.ops.forEach { op ->
            when (RemoteOpType.parse(op.type)) {
                RemoteOpType.RECT -> paintRect(op, palette)
                RemoteOpType.ARC -> paintArc(op, variables, palette)
                RemoteOpType.TEXT -> paintText(op, variables, palette, textMeasurer)
                null -> Unit
            }
        }
    }

    private fun DrawScope.paintRect(
        op: RemoteOp,
        palette: IPalette,
    ) {
        val color = RemoteColorToken.resolve(op.color, palette).withAlpha(op.alpha)
        val topLeft = Offset(op.x * size.width, op.y * size.height)
        val rectSize = Size(op.w * size.width, op.h * size.height)
        if (op.stroke > 0f) {
            drawRect(color, topLeft, rectSize, style = Stroke(op.stroke * minOf(rectSize.width, rectSize.height)))
        } else {
            drawRect(color, topLeft, rectSize)
        }
    }

    private fun DrawScope.paintArc(
        op: RemoteOp,
        variables: RemoteVariables,
        palette: IPalette,
    ) {
        val color = RemoteColorToken.resolve(op.color, palette).withAlpha(op.alpha)
        val boundsW = op.w * size.width
        val boundsH = op.h * size.height
        val diameter = minOf(boundsW, boundsH)
        val strokeWidth = op.stroke * diameter
        // Keep the ring fully inside its bounds: the stroke is centered on the arc path.
        val arcDiameter = (diameter - strokeWidth).coerceAtLeast(0f)
        val topLeft =
            Offset(
                op.x * size.width + (boundsW - arcDiameter) / 2f,
                op.y * size.height + (boundsH - arcDiameter) / 2f,
            )
        val ratio = variables.numbers[op.sweepVar]?.coerceIn(0f, 1f)
        val sweep = if (op.sweepVar.isEmpty() || ratio == null) op.sweep else ratio * RemoteOp.FULL_CIRCLE
        drawArc(
            color = color,
            startAngle = op.startAngle,
            sweepAngle = sweep,
            useCenter = op.stroke <= 0f,
            topLeft = topLeft,
            size = Size(arcDiameter, arcDiameter),
            style = if (op.stroke > 0f) Stroke(strokeWidth) else Fill,
        )
    }

    private fun DrawScope.paintText(
        op: RemoteOp,
        variables: RemoteVariables,
        palette: IPalette,
        textMeasurer: TextMeasurer,
    ) {
        val text = op.text.resolve(variables)
        if (text.isEmpty()) return
        val color = RemoteColorToken.resolve(op.color, palette).withAlpha(op.alpha)
        val style = TextStyle(color = color, fontSize = (op.textSize * size.height).toSp())
        val measured = textMeasurer.measure(text, style)
        val boundsX = op.x * size.width
        val boundsW = op.w * size.width
        val left =
            when (RemoteAlign.parse(op.align)) {
                RemoteAlign.TOP_START, RemoteAlign.CENTER_START, RemoteAlign.BOTTOM_START -> boundsX
                RemoteAlign.TOP_END, RemoteAlign.CENTER_END, RemoteAlign.BOTTOM_END ->
                    boundsX + boundsW - measured.size.width
                else -> boundsX + (boundsW - measured.size.width) / 2f
            }
        val top = op.y * size.height + (op.h * size.height - measured.size.height) / 2f
        drawText(textMeasurer, text, Offset(left, top), style)
    }

    /** Replaces every `{name}` with its value from [variables]; unknown names stay empty. */
    private fun String.resolve(variables: RemoteVariables): String {
        var result = this
        variables.texts.forEach { (name, value) ->
            result = result.replace(PLACEHOLDER_START + name + PLACEHOLDER_END, value)
        }
        return result
    }

    private fun Color.withAlpha(multiplier: Float) =
        copy(alpha = alpha * multiplier.coerceIn(0f, 1f))
}
