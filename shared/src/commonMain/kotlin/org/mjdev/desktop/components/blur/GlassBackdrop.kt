package org.mjdev.desktop.components.blur

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.image.ImageAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.Colors.ContrastColorFilter
import kotlin.math.roundToInt

/** Constants of [GlassBackdrop]. */
private object GlassDefaults {
    /** Blur radius of the frosted glass. */
    val blurRadius: Dp = 28.dp

    /** Same contrast the desktop applies to the wallpaper, so glass and wallpaper match. */
    const val WALLPAPER_CONTRAST = 1.5f
}

/**
 * Frosted glass that works on the JVM without any compositor support. The desktop window draws the
 * wallpaper, so every other shell window paints its own copy of it, lines it up with where the
 * window really sits on the screen (see [LocalWindowBounds]) and blurs it. Only the wallpaper can
 * be blurred this way, never other applications' windows. Draws nothing until a wallpaper is known.
 */
@Suppress("FunctionName")
@Composable
fun GlassBackdrop(
    modifier: Modifier = Modifier,
    blurRadius: Dp = GlassDefaults.blurRadius,
    shape: Shape = RectangleShape,
) = withDesktopContext {
    val wallpaper = context.wallpaper.value
    val windowPosition = LocalWindowBounds.current.position
    // top left corner of this node inside its window, in px
    var origin by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier =
            modifier
                .clip(shape)
                .onGloballyPositioned { origin = it.positionInWindow() }
                .blur(blurRadius, BlurredEdgeTreatment.Rectangle),
    ) {
        when (wallpaper) {
            null -> Unit
            is Color -> Box(Modifier.fillMaxSize().background(wallpaper))
            else ->
                Box(
                    modifier =
                        Modifier.fillMaxSize().layout { measurable, constraints ->
                            // the copy is as big as the screen and shifted so the pixels under this
                            // node are exactly the wallpaper pixels that are on screen there
                            val screen =
                                Constraints.fixed(
                                    containerSize.width.roundToPx(),
                                    containerSize.height.roundToPx(),
                                )
                            val placeable = measurable.measure(screen)
                            layout(constraints.maxWidth, constraints.maxHeight) {
                                placeable.place(
                                    -(windowPosition.x.roundToPx() + origin.x.roundToInt()),
                                    -(windowPosition.y.roundToPx() + origin.y.roundToInt()),
                                )
                            }
                        },
                ) {
                    ImageAny(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader,
                        src = wallpaper,
                        colorFilter = ContrastColorFilter(GlassDefaults.WALLPAPER_CONTRAST),
                    )
                }
        }
    }
}
