/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.compose

import androidx.compose.ui.Modifier

import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints

@Stable
fun Modifier.zIndex(
    zIndex: Float
): Modifier = this then ZIndexElement(zIndex = zIndex)

data class ZIndexElement(val zIndex: Float) : ModifierNodeElement<ZIndexNode>() {
    override fun create() = ZIndexNode(zIndex)
    override fun update(node: ZIndexNode) {
        node.zIndex = zIndex
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "zIndex"
        properties["zIndex"] = zIndex
    }
}

class ZIndexNode(var zIndex: Float) : LayoutModifierNode, Modifier.Node() {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0, zIndex = zIndex)
        }
    }

    override fun toString(): String = "ZIndexModifier(zIndex=$zIndex)"
}
