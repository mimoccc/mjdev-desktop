/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.card.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.surface.base.*

@Suppress("ConstPropertyName", "unused", "MemberVisibilityCanBePrivate")
object CardDefaults {
    val ContentImageAlignment = Alignment.Center
    val ContainerShape = RoundedCornerShape(8.dp)

    const val SquareImageAspectRatio = 1f
    const val VerticalImageAspectRatio = 2f / 3
    const val HorizontalImageAspectRatio = 16f / 9

    const val SubtitleAlpha = 0.6f
    const val DescriptionAlpha = 0.8f

    val ScrimBrush = Brush.verticalGradient(
        listOf(
            Color(red = 28, green = 27, blue = 31, alpha = 0),
            Color(red = 28, green = 27, blue = 31, alpha = 204)
        )
    )

    fun shape(
        shape: Shape = ContainerShape,
        focusedShape: Shape = shape,
        pressedShape: Shape = shape
    ) = CardShape(shape = shape, focusedShape = focusedShape, pressedShape = pressedShape)

    @ReadOnlyComposable
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor: Color = contentColorFor(containerColor),
        focusedContainerColor: Color = containerColor,
        focusedContentColor: Color = contentColorFor(focusedContainerColor),
        pressedContainerColor: Color = focusedContainerColor,
        pressedContentColor: Color = contentColorFor(pressedContainerColor)
    ) = CardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        focusedContainerColor = focusedContainerColor,
        focusedContentColor = focusedContentColor,
        pressedContainerColor = pressedContainerColor,
        pressedContentColor = pressedContentColor
    )

    @ReadOnlyComposable
    @Composable
    fun compactCardColors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor: Color = Color.White,
        focusedContainerColor: Color = containerColor,
        focusedContentColor: Color = contentColor,
        pressedContainerColor: Color = focusedContainerColor,
        pressedContentColor: Color = focusedContentColor
    ) = CardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        focusedContainerColor = focusedContainerColor,
        focusedContentColor = focusedContentColor,
        pressedContainerColor = pressedContainerColor,
        pressedContentColor = pressedContentColor
    )

    @Composable
    fun CardDefaults.colorFocusBorder(
        colorFocused: Color = Color.Green,
        colorUnFocused: Color = Color.Black,
        colorPressed: Color = Color.Yellow,
        width: Dp = 1.dp,
        roundCornerSize: Dp = 8.dp
    ) = border(
        border = Border(
            border = BorderStroke(
                width = width,
                color = colorUnFocused
            ),
            shape = RoundedCornerShape(roundCornerSize)
        ),
        focusedBorder = Border(
            border = BorderStroke(
                width = width,
                color = colorFocused
            ),
            shape = RoundedCornerShape(roundCornerSize)
        ),
    pressedBorder = Border(
        border = BorderStroke(
            width = width,
            color = colorPressed
        ),
        shape = RoundedCornerShape(roundCornerSize)
    )
    )

    @Composable
    fun CardDefaults.colorFocusGlow(
        focusColor: Color = Color.Green,
        onUnFocusColor: Color = Color.Transparent,
        elevation: Dp = 10.dp
    ): CardGlow = glow(
        glow = Glow(
            elevationColor = onUnFocusColor,
            elevation = elevation
        ),
        focusedGlow = Glow(
            elevationColor = focusColor,
            elevation = elevation
        ),
        pressedGlow = Glow(
            elevationColor = onUnFocusColor,
            elevation = elevation
        )
    )

    fun scale(
        scale: Float = 1f,
        focusedScale: Float = 1.1f,
        pressedScale: Float = scale
    ) = CardScale(scale = scale, focusedScale = focusedScale, pressedScale = pressedScale)

    @ReadOnlyComposable
    @Composable
    fun border(
        border: Border = Border.None,
        focusedBorder: Border = Border(
            border = BorderStroke(width = 3.dp, color = Color.White), // todo theme
            shape = ContainerShape
        ),
        pressedBorder: Border = focusedBorder
    ) = CardBorder(
        border = border,
        focusedBorder = focusedBorder,
        pressedBorder = pressedBorder
    )

    fun glow(
        glow: Glow = Glow.None,
        focusedGlow: Glow = glow,
        pressedGlow: Glow = glow
    ) = CardGlow(
        glow = glow,
        focusedGlow = focusedGlow,
        pressedGlow = pressedGlow
    )

    fun CardColors.toClickableSurfaceColors() = ClickableSurfaceColors(
        containerColor = containerColor,
        contentColor = contentColor,
        focusedContainerColor = focusedContainerColor,
        focusedContentColor = focusedContentColor,
        pressedContainerColor = pressedContainerColor,
        pressedContentColor = pressedContentColor,
        disabledContainerColor = containerColor,
        disabledContentColor = contentColor
    )

    fun CardShape.toClickableSurfaceShape() =
        ClickableSurfaceShape(
            shape = shape,
            focusedShape = focusedShape,
            pressedShape = pressedShape,
            disabledShape = shape,
            focusedDisabledShape = shape
        )

    fun CardScale.toClickableSurfaceScale() =
        ClickableSurfaceScale(
            scale = scale,
            focusedScale = focusedScale,
            pressedScale = pressedScale,
            disabledScale = scale,
            focusedDisabledScale = scale
        )

    fun CardBorder.toClickableSurfaceBorder() =
        ClickableSurfaceBorder(
            border = border,
            focusedBorder = focusedBorder,
            pressedBorder = pressedBorder,
            disabledBorder = border,
            focusedDisabledBorder = border
        )

    fun CardGlow.toClickableSurfaceGlow() =
        ClickableSurfaceGlow(glow = glow, focusedGlow = focusedGlow, pressedGlow = pressedGlow)
}