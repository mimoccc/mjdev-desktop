/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.surface.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object ClickableSurfaceDefaults {
    internal fun shape(
        enabled: Boolean,
        focused: Boolean,
        pressed: Boolean,
        shape: ClickableSurfaceShape
    ): Shape {
        return when {
            pressed && enabled -> shape.pressedShape
            focused && enabled -> shape.focusedShape
            focused && !enabled -> shape.focusedDisabledShape
            enabled -> shape.shape
            else -> shape.disabledShape
        }
    }

    @ReadOnlyComposable
    @Composable
    fun shape(
        shape: Shape = MaterialTheme.shapes.medium,
        focusedShape: Shape = shape,
        pressedShape: Shape = shape,
        disabledShape: Shape = shape,
        focusedDisabledShape: Shape = disabledShape
    ) = ClickableSurfaceShape(
        shape = shape,
        focusedShape = focusedShape,
        pressedShape = pressedShape,
        disabledShape = disabledShape,
        focusedDisabledShape = focusedDisabledShape
    )

    internal fun containerColor(
        enabled: Boolean,
        focused: Boolean,
        pressed: Boolean,
        colors: ClickableSurfaceColors
    ): Color = when {
        pressed && enabled -> colors.pressedContainerColor
        focused && enabled -> colors.focusedContainerColor
        enabled -> colors.containerColor
        else -> colors.disabledContainerColor
    }

    internal fun contentColor(
        enabled: Boolean,
        focused: Boolean,
        pressed: Boolean,
        colors: ClickableSurfaceColors
    ): Color = when {
        pressed && enabled -> colors.pressedContentColor
        focused && enabled -> colors.focusedContentColor
        enabled -> colors.contentColor
        else -> colors.disabledContentColor
    }

    @Suppress("ConstPropertyName")
    private const val DisabledContainerAlpha = 0.5f

    @ReadOnlyComposable
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = contentColorFor(containerColor),
        focusedContainerColor: Color = MaterialTheme.colorScheme.inverseSurface,
        focusedContentColor: Color = contentColorFor(focusedContainerColor),
        pressedContainerColor: Color = focusedContainerColor,
        pressedContentColor: Color = contentColorFor(pressedContainerColor),
        disabledContainerColor: Color =
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = DisabledContainerAlpha),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface
    ) = ClickableSurfaceColors(
        containerColor = containerColor,
        contentColor = contentColor,
        focusedContainerColor = focusedContainerColor,
        focusedContentColor = focusedContentColor,
        pressedContainerColor = pressedContainerColor,
        pressedContentColor = pressedContentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    internal fun scale(
        enabled: Boolean,
        focused: Boolean,
        pressed: Boolean,
        scale: ClickableSurfaceScale
    ): Float = when {
        pressed && enabled -> scale.pressedScale
        focused && enabled -> scale.focusedScale
        focused && !enabled -> scale.focusedDisabledScale
        enabled -> scale.scale
        else -> scale.disabledScale
    }

    fun scale(
        scale: Float = 1f,
        focusedScale: Float = 1.1f,
        pressedScale: Float = scale,
        disabledScale: Float = scale,
        focusedDisabledScale: Float = disabledScale
    ) = ClickableSurfaceScale(
        scale = scale,
        focusedScale = focusedScale,
        pressedScale = pressedScale,
        disabledScale = disabledScale,
        focusedDisabledScale = focusedDisabledScale
    )

    internal fun border(
        enabled: Boolean,
        focused: Boolean,
        pressed: Boolean,
        border: ClickableSurfaceBorder
    ): Border = when {
        pressed && enabled -> border.pressedBorder
        focused && enabled -> border.focusedBorder
        focused && !enabled -> border.focusedDisabledBorder
        enabled -> border.border
        else -> border.disabledBorder
    }

    @ReadOnlyComposable
    @Composable
    fun border(
        border: Border = Border.None,
        focusedBorder: Border = border,
        pressedBorder: Border = focusedBorder,
        disabledBorder: Border = border,
        focusedDisabledBorder: Border = Border(
            border = BorderStroke(width = 2.dp, color = Color.White), // todo theme
            inset = 0.dp,
            shape = ShapeDefaults.Small
        )
    ) = ClickableSurfaceBorder(
        border = border,
        focusedBorder = focusedBorder,
        pressedBorder = pressedBorder,
        disabledBorder = disabledBorder,
        focusedDisabledBorder = focusedDisabledBorder
    )

    internal fun glow(
        enabled: Boolean,
        focused: Boolean,
        pressed: Boolean,
        glow: ClickableSurfaceGlow
    ): Glow = if (enabled) {
        when {
            pressed -> glow.pressedGlow
            focused -> glow.focusedGlow
            else -> glow.glow
        }
    } else {
        Glow.None
    }

    fun glow(
        glow: Glow = Glow.None,
        focusedGlow: Glow = glow,
        pressedGlow: Glow = glow
    ) = ClickableSurfaceGlow(glow = glow, focusedGlow = focusedGlow, pressedGlow = pressedGlow)
}
