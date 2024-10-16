/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.surface

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.LocalContentColor
//import androidx.compose.material3.LocalAbsoluteTonalElevation
//import androidx.compose.material3.LocalContentColor
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.surface.base.*
import eu.mjdev.desktop.extensions.Compose.onMouseLongPress
import eu.mjdev.desktop.extensions.Compose.onMousePress
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Modifier.conditional
import eu.mjdev.desktop.helpers.compose.surfaceBorder
import eu.mjdev.desktop.helpers.compose.zIndex

@Suppress("SameParameterValue")
@Composable
fun Surface(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    tonalElevation: Dp = 0.dp,
    shape: ClickableSurfaceShape = ClickableSurfaceDefaults.shape(),
    colors: ClickableSurfaceColors = ClickableSurfaceDefaults.colors(),
    scale: ClickableSurfaceScale = ClickableSurfaceDefaults.scale(),
    border: ClickableSurfaceBorder = ClickableSurfaceDefaults.border(),
    glow: ClickableSurfaceGlow = ClickableSurfaceDefaults.glow(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    @Suppress("NAME_SHADOWING") val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    SurfaceImpl(
        modifier = modifier.onMousePress {
            if (enabled) onClick()
        }.onMouseLongPress {
            if (enabled) onLongClick?.invoke()
        },
        selected = false,
        enabled = enabled,
        tonalElevation = tonalElevation,
        shape = ClickableSurfaceDefaults.shape(
            enabled = enabled, focused = focused, pressed = pressed, shape = shape
        ),
        color = ClickableSurfaceDefaults.containerColor(
            enabled = enabled, focused = focused, pressed = pressed, colors = colors
        ),
        contentColor = ClickableSurfaceDefaults.contentColor(
            enabled = enabled, focused = focused, pressed = pressed, colors = colors
        ),
        scale = ClickableSurfaceDefaults.scale(
            enabled = enabled, focused = focused, pressed = pressed, scale = scale
        ),
        border = ClickableSurfaceDefaults.border(
            enabled = enabled, focused = focused, pressed = pressed, border = border
        ),
        glow = ClickableSurfaceDefaults.glow(
            enabled = enabled, focused = focused, pressed = pressed, glow = glow
        ),
        interactionSource = interactionSource,
        content = content
    )
}

@Suppress("SameParameterValue")
@Composable
private fun SurfaceImpl(
    modifier: Modifier,
    selected: Boolean,
    enabled: Boolean,
    shape: Shape,
    color: Color,
    contentColor: Color,
    scale: Float,
    border: Border,
    glow: Glow,
    tonalElevation: Dp,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable (BoxScope.() -> Unit)
) {
    @Suppress("NAME_SHADOWING") val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val surfaceAlpha = stateAlpha(enabled = enabled, focused = focused, pressed = pressed, selected = selected)
//    val absoluteElevation =
//        LocalAbsoluteTonalElevation.current + tonalElevation
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
//        LocalAbsoluteTonalElevation provides absoluteElevation
    ) {
        val zIndex by animateFloatAsState(
            targetValue = if (focused) FocusedZIndex else NonFocusedZIndex, label = "zIndex"
        )
        val backgroundColorByState = surfaceColorAtElevation(
            color = color,
//            elevation = tonalElevation, // LocalAbsoluteTonalElevation.current
        )
        Box(
            modifier = modifier.tvSurfaceScale(
                scale = scale,
                interactionSource = interactionSource,
            ).surfaceGlow(shape, glow).zIndex(zIndex).conditional(border != Border.None) {
                surfaceBorder(shape, border)
            }.background(backgroundColorByState, shape).graphicsLayer {
                this.alpha = surfaceAlpha
                this.shape = shape
                this.clip = true
                this.compositingStrategy = CompositingStrategy.Offscreen
            }, propagateMinConstraints = true
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    this.alpha = if (!enabled) DisabledContentAlpha else EnabledContentAlpha
                }, content = content
            )
        }
    }
}

// todo elevation
@Composable
fun surfaceColorAtElevation(
    color: Color,
//    elevation: Dp
): Color {
//    if (color == MaterialTheme.colorScheme.surface) {
//        return MaterialTheme.colorScheme.surfaceColorAtElevation(elevation)
//    } else {
    return color
//    }
}

@Suppress("ConstPropertyName")
object SurfaceScaleTokens {
    const val focusDuration: Int = 300
    const val unFocusDuration: Int = 500
    const val pressedDuration: Int = 120
    const val releaseDuration: Int = 300
    val enterEasing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
}

@Composable
fun Modifier.tvSurfaceScale(
    scale: Float,
    interactionSource: MutableInteractionSource,
): Modifier {
    val interaction by interactionSource.interactions.collectAsState(initial = FocusInteraction.Focus())
    val animationSpec = defaultScaleAnimationSpec(interaction)
    val animatedScale by animateFloatAsState(
        targetValue = scale, animationSpec = animationSpec, label = "tv-surface-scale"
    )
    return this.graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
}

fun defaultScaleAnimationSpec(interaction: Interaction): TweenSpec<Float> = tween(
    durationMillis = when (interaction) {
        is FocusInteraction.Focus -> SurfaceScaleTokens.focusDuration
        is FocusInteraction.Unfocus -> SurfaceScaleTokens.unFocusDuration
        is PressInteraction.Press -> SurfaceScaleTokens.pressedDuration
        is PressInteraction.Release -> SurfaceScaleTokens.releaseDuration
        is PressInteraction.Cancel -> SurfaceScaleTokens.releaseDuration
        else -> SurfaceScaleTokens.releaseDuration
    }, easing = SurfaceScaleTokens.enterEasing
)

fun stateAlpha(
    enabled: Boolean, focused: Boolean, pressed: Boolean, selected: Boolean
): Float {
    return when {
        !enabled && pressed -> DisabledPressedStateAlpha
        !enabled && focused -> DisabledFocusedStateAlpha
        !enabled && selected -> DisabledSelectedStateAlpha
        enabled -> EnabledContentAlpha
        else -> DisabledDefaultStateAlpha
    }
}

private const val DisabledPressedStateAlpha = 0.8f
private const val DisabledFocusedStateAlpha = 0.8f
private const val DisabledSelectedStateAlpha = 0.8f
private const val DisabledDefaultStateAlpha = 0.6f

private const val DisabledContentAlpha = 0.8f
internal const val EnabledContentAlpha = 1f

private const val FocusedZIndex = 0.5f
private const val NonFocusedZIndex = 0f

// todo
@Preview
@Composable
fun SurfacePreview() = preview {
    Surface()
}
