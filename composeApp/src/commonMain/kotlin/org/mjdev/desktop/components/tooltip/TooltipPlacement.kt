package org.mjdev.desktop.components.tooltip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.PopupPositionProvider
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

interface TooltipPlacement {
    @Composable
    fun positionProvider(cursorPosition: Offset): PopupPositionProvider

    class CursorPoint(
        private val offset: DpOffset = DpOffset.Zero,
        private val alignment: Alignment = Alignment.BottomEnd,
        private val windowMargin: Dp = 4.dp,
    ) : TooltipPlacement {
        @Composable
        override fun positionProvider(cursorPosition: Offset) =
            rememberPopupPositionProviderAtPosition(
                positionPx = cursorPosition,
                offset = offset,
                alignment = alignment,
                windowMargin = windowMargin,
            )
    }

    class ComponentRect(
        private val anchor: Alignment = Alignment.BottomCenter,
        private val alignment: Alignment = Alignment.BottomCenter,
        private val offset: DpOffset = DpOffset.Zero,
    ) : TooltipPlacement {
        @Composable
        override fun positionProvider(cursorPosition: Offset) =
            rememberComponentRectPositionProvider(
                anchor = anchor,
                alignment = alignment,
                offset = offset,
            )
    }
}

class PopupPositionProviderAtPosition(
    val positionPx: Offset,
    val isRelativeToAnchor: Boolean,
    val offsetPx: Offset,
    val alignment: Alignment = Alignment.BottomEnd,
    val windowMarginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val anchor =
            IntRect(
                offset =
                    positionPx.round() +
                        (if (isRelativeToAnchor) anchorBounds.topLeft else IntOffset.Zero),
                size = IntSize.Zero,
            )
        val tooltipArea =
            IntRect(
                IntOffset(
                    anchor.left - popupContentSize.width,
                    anchor.top - popupContentSize.height,
                ),
                IntSize(
                    popupContentSize.width * 2,
                    popupContentSize.height * 2,
                ),
            )
        val position = alignment.align(popupContentSize, tooltipArea.size, layoutDirection)
        var x = tooltipArea.left + position.x + offsetPx.x
        var y = tooltipArea.top + position.y + offsetPx.y
        if (x + popupContentSize.width > windowSize.width - windowMarginPx) {
            x -= popupContentSize.width
        }
        if (y + popupContentSize.height > windowSize.height - windowMarginPx) {
            y -= popupContentSize.height + anchor.height
        }
        x = x.coerceAtLeast(windowMarginPx.toFloat())
        y = y.coerceAtLeast(windowMarginPx.toFloat())

        return IntOffset(x.roundToInt(), y.roundToInt())
    }
}

@Composable
fun rememberPopupPositionProviderAtPosition(
    positionPx: Offset,
    offset: DpOffset = DpOffset.Zero,
    alignment: Alignment = Alignment.BottomEnd,
    windowMargin: Dp = 4.dp,
): PopupPositionProvider =
    with(LocalDensity.current) {
        val offsetPx = Offset(offset.x.toPx(), offset.y.toPx())
        val windowMarginPx = windowMargin.roundToPx()

        remember(positionPx, offsetPx, alignment, windowMarginPx) {
            PopupPositionProviderAtPosition(
                positionPx = positionPx,
                isRelativeToAnchor = true,
                offsetPx = offsetPx,
                alignment = alignment,
                windowMarginPx = windowMarginPx,
            )
        }
    }

@Composable
fun rememberComponentRectPositionProvider(
    anchor: Alignment = Alignment.BottomCenter,
    alignment: Alignment = Alignment.BottomCenter,
    offset: DpOffset = DpOffset.Zero,
): PopupPositionProvider {
    val offsetPx =
        with(LocalDensity.current) {
            IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
        }
    return remember(anchor, alignment, offsetPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val anchorPoint = anchor.align(IntSize.Zero, anchorBounds.size, layoutDirection)
                val tooltipArea =
                    IntRect(
                        IntOffset(
                            anchorBounds.left + anchorPoint.x - popupContentSize.width,
                            anchorBounds.top + anchorPoint.y - popupContentSize.height,
                        ),
                        IntSize(
                            popupContentSize.width * 2,
                            popupContentSize.height * 2,
                        ),
                    )
                val position = alignment.align(popupContentSize, tooltipArea.size, layoutDirection)
                return tooltipArea.topLeft + position + offsetPx
            }
        }
    }
}

// todo previews
