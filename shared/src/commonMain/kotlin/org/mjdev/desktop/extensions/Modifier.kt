package org.mjdev.desktop.extensions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("unused")
@OptIn(ExperimentalComposeUiApi::class)
object Modifier {
    @Composable
    fun Modifier.conditional(
        condition: Boolean,
        onFalse: (Modifier.() -> Modifier)? = null,
        onTrue: Modifier.() -> Modifier,
    ): Modifier =
        if (condition) {
            then(onTrue(Modifier))
        } else if (onFalse != null) {
            then(onFalse(Modifier))
        } else {
            this
        }

    fun Modifier.onKey(
        keyCode: Key,
        action: KeyEventType = KeyEventType.KeyDown,
        block: () -> Unit,
    ): Modifier =
        this then
            onKeyEvent { ev ->
                if (ev.type == action) {
                    if (ev.key == keyCode) {
                        block()
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }

    @Composable
    fun Modifier.scaleOnPress(
        interactionSource: InteractionSource,
        pressScale: Float = 0.95f,
        leaveScale: Float = 1f,
    ) = composed {
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(if (isPressed) pressScale else leaveScale)
        this.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    }

    @Composable
    fun Modifier.size(
        width: Int,
        height: Int,
    ) = size(width.dp, height.dp)

    @Composable
    fun Modifier.size(value: Int) = size(value.dp, value.dp)

    @Composable
    fun Modifier.dashedBorder(
        strokeWidth: Dp,
        color: Color,
        cornerRadiusDp: Dp,
    ) = composed(
        factory = {
            val density = LocalDensity.current
            val strokeWidthPx = density.run { strokeWidth.toPx() }
            val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }
            then(
                Modifier.drawWithCache {
                    onDrawBehind {
                        val stroke =
                            Stroke(
                                width = strokeWidthPx,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                            )
                        drawRoundRect(
                            color = color,
                            style = stroke,
                            cornerRadius = CornerRadius(cornerRadiusPx),
                        )
                    }
                },
            )
        },
    )

    @Composable
    fun Modifier.onPointerEvent(
        eventType: PointerEventType,
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit,
    ): Modifier =
        composed {
            val currentEventType by rememberUpdatedState(eventType)
            val currentOnEvent by rememberUpdatedState(onEvent)
            pointerInput(pass) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass)
                        if (event.type == currentEventType) {
                            currentOnEvent(event)
                        }
                    }
                }
            }
        }

    @Composable
    fun Modifier.onMouseEnter(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit,
    ): Modifier =
        onPointerEvent(
            eventType = PointerEventType.Enter,
            pass = pass,
            onEvent = onEvent,
        )

    @Composable
    fun Modifier.onMouseLeave(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit,
    ): Modifier =
        onPointerEvent(
            eventType = PointerEventType.Exit,
            pass = pass,
            onEvent = onEvent,
        )

    @Composable
    fun Modifier.onMousePress(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit,
    ): Modifier =
        onPointerEvent(
            eventType = PointerEventType.Release,
            pass = pass,
            onEvent = onEvent,
        )

    @Composable
    fun Modifier.onMouseLongPress(
        pass: PointerEventPass = PointerEventPass.Main,
        onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit,
    ): Modifier {
//        return onPointerEvent(
//            eventType = PointerEventType.Press,
//            pass = pass,
//            onEvent = onEvent
//        )
        return this
    }

    val AwaitPointerEventScope.isPrimary
        get() = !isSecondary

    val AwaitPointerEventScope.isSecondary
        get() = currentEvent.buttons.isSecondaryPressed

    fun AwaitPointerEventScope.onLeftClick(block: AwaitPointerEventScope.() -> Unit) {
        if (isPrimary) block()
    }

    fun AwaitPointerEventScope.onRightClick(block: AwaitPointerEventScope.() -> Unit) {
        if (isSecondary) block()
    }

//    val PointerEvent.isConsumed
//        get() = changes.firstOrNull()?.isConsumed ?: false

//    val PointerEvent.isNotConsumed
//        get() = isConsumed.not()

//    val PointerEvent.isRelease
//        get() = type == PointerEventType.Release

//    val PointerEvent.isPress
//        get() = type == PointerEventType.Press

//    val PointerEvent.isPressed
//        get() = changes.firstOrNull()?.pressed ?: false

//    @OptIn(ExperimentalComposeUiApi::class)
//    val PointerEvent.isLeftButton
//        get() = buttons.isPrimaryPressed

//    val PointerEvent.isRightButton
//        get() = buttons.isSecondaryPressed

//    val PointerEvent.isNotPressed
//        get() = isPressed.not()

//    fun Modifier.onLeftClick(
//        onClick: () -> Unit
//    ): Modifier = pointerInput(Unit) {
//        awaitPointerEventScope {
//            while (true) {
//                val event = awaitPointerEvent(PointerEventPass.Main)
//                if (event.isPressed && event.isNotConsumed && event.isLeftButton) {
//                    onClick()
//                }
//            }
//        }
//    }

//    fun Modifier.onRightClick(
//        onClick: () -> Unit
//    ): Modifier = pointerInput(Unit) {
//        awaitPointerEventScope {
//            while (true) {
//                val event = awaitPointerEvent(PointerEventPass.Main)
//                if (event.isPressed && event.isNotConsumed && event.isRightButton) {
//                    onClick()
//                }
//            }
//        }
//    }

    @Stable
    @Composable
    fun Modifier.clipCircle() = clip(CircleShape)

    @Stable
    @Composable
    fun Modifier.clipRect() = clip(RectangleShape)

    @Stable
    @Composable
    fun Modifier.clipRoundRect(round: Dp) = clip(RoundedCornerShape(round))

    @Stable
    @Composable
    fun Modifier.circleBorder(
        width: Dp = 2.dp,
        color: Color = Color.White,
    ) = clipCircle().border(width, color, CircleShape)
}
