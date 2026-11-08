package org.psippr.desktop.components.desktop.widgets.music

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.psippr.desktop.plugins.music.MusicDefaults

/**
 * Thin seek bar of the music card: played part and a small thumb in [accentColor], the rest
 * faded. Tap or drag to seek; [onSeek] gets the chosen fraction (0..1) when the gesture ends.
 */
@Suppress("FunctionName")
@Composable
fun MusicProgressBar(
    progress: Float,
    enabled: Boolean,
    accentColor: Color,
    onSeek: (Float) -> Unit,
) {
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val shown = (dragFraction ?: progress).coerceIn(0f, 1f)
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(MusicDefaults.PROGRESS_TOUCH_DP.dp)
                .pointerInput(enabled) {
                    if (enabled) detectTapGestures { tap -> onSeek(fraction(tap.x, size.width)) }
                }.pointerInput(enabled) {
                    if (enabled) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                dragFraction?.let(onSeek)
                                dragFraction = null
                            },
                            onDragCancel = { dragFraction = null },
                        ) { change, _ ->
                            change.consume()
                            dragFraction = fraction(change.position.x, size.width)
                        }
                    }
                },
    ) {
        val y = size.height / 2
        val track = MusicDefaults.PROGRESS_TRACK_DP.dp.toPx()
        val playedX = size.width * shown
        drawLine(
            color = accentColor.copy(alpha = MusicDefaults.PROGRESS_REST_ALPHA),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = track,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = accentColor,
            start = Offset(0f, y),
            end = Offset(playedX, y),
            strokeWidth = track,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = accentColor,
            radius = MusicDefaults.PROGRESS_THUMB_DP.dp.toPx() / 2,
            center = Offset(playedX, y),
        )
    }
}

// position [x] on a bar of [width] as a 0..1 fraction
private fun fraction(
    x: Float,
    width: Int,
): Float = if (width > 0) (x / width).coerceIn(0f, 1f) else 0f
