/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.blur

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.mjdev.desktop.components.image.ImageAny
import org.mjdev.desktop.composeapp.generated.resources.Res
import org.mjdev.desktop.composeapp.generated.resources.blur4
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.jetbrains.compose.ui.tooling.preview.Preview

// todo real blur
@Composable
fun BlurPanel(
    modifier: Modifier = Modifier,
    resourceId: DrawableResource = Res.drawable.blur4,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    blurRadius: Dp = 12.dp,
    alpha: Float = 0.3f,
    content: @Composable BoxScope.() -> Unit = {}
) = withDesktopContext {
    val transparentGradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0x66FFFFFF),
            Color(0x1AFFFFFF)
        )
    )
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints
    ) {
        ImageAny(
            modifier = modifier.fillMaxSize()
                .alpha(alpha)
                .background(transparentGradientBrush)
                .blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded),
            src = painterResource(resourceId),
            contentScale = if (maxHeight > maxWidth) ContentScale.FillHeight else ContentScale.FillWidth
        )
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.Transparent,
                            backgroundColor.alpha(alpha + 0.2f)
                        )
                    )
                )
        )
        content()
    }
}

@Preview
@Composable
fun BlurPanelPreview() = preview(480, 800) {
    BlurPanel()
}
