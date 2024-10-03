/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.debugInspectorInfo
import eu.mjdev.desktop.components.surface.base.Border
import eu.mjdev.desktop.components.surface.base.ShapeTokens
import eu.mjdev.desktop.components.surface.base.SurfaceShapeOutlineCache

internal fun Modifier.surfaceBorder(
    shape: Shape,
    border: Border,
): Modifier {
    return then(
        SurfaceBorderElement(
            shape = shape,
            border = border,
            inspectorInfo =
                debugInspectorInfo {
                    name = "tvSurfaceBorder"
                    properties["shape"] = shape
                    properties["border"] = border
                }
        )
    )
}

private class SurfaceBorderElement(
    private val shape: Shape,
    private val border: Border,
    private val inspectorInfo: InspectorInfo.() -> Unit
) : ModifierNodeElement<SurfaceBorderNode>() {
    override fun create(): SurfaceBorderNode {
        return SurfaceBorderNode(
            shape = shape,
            border = border,
        )
    }

    override fun update(node: SurfaceBorderNode) {
        node.reactToUpdates(
            newShape = shape,
            newBorder = border,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        inspectorInfo()
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + border.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        val otherTyped = other as? SurfaceBorderElement ?: return false
        return shape == otherTyped.shape && border == otherTyped.border
    }
}

private class SurfaceBorderNode(
    private var shape: Shape,
    private var border: Border,
) : DrawModifierNode, Modifier.Node() {

    private var shapeOutlineCache: SurfaceShapeOutlineCache? = null
    private var outlineStrokeCache: OutlineStrokeCache? = null

    fun reactToUpdates(
        newShape: Shape,
        newBorder: Border,
    ) {
        shape = newShape
        border = newBorder
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val borderStroke = border.border
        val borderShape = if (border.shape == ShapeTokens.BorderDefaultShape) shape else border.shape
        if (shapeOutlineCache == null) {
            shapeOutlineCache =
                SurfaceShapeOutlineCache(
                    shape = borderShape,
                    size = size,
                    layoutDirection = layoutDirection,
                    density = this
                )
        }
        if (outlineStrokeCache == null) {
            outlineStrokeCache = OutlineStrokeCache(widthPx = borderStroke.width.toPx())
        }
        inset(inset = -border.inset.toPx()) {
            val shapeOutline =
                shapeOutlineCache!!.updatedOutline(
                    shape = borderShape,
                    size = size,
                    layoutDirection = layoutDirection,
                    density = this
                )
            val outlineStroke = outlineStrokeCache!!.updatedOutlineStroke(widthPx = borderStroke.width.toPx())
            drawOutline(
                outline = shapeOutline,
                brush = borderStroke.brush,
                alpha = 1f,
                style = outlineStroke
            )
        }
    }
}

private class OutlineStrokeCache(private var widthPx: Float) {
    private var outlineStroke: Stroke? = null

    fun updatedOutlineStroke(widthPx: Float): Stroke {
        if (outlineStroke == null || this.widthPx != widthPx) {
            this.widthPx = widthPx
            createOutlineStroke()
        }
        return outlineStroke!!
    }

    private fun createOutlineStroke() {
        outlineStroke = Stroke(width = widthPx, cap = StrokeCap.Round)
    }
}
