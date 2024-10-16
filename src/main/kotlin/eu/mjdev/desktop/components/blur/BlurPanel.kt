/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.blur

import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

// todo real blur
@Composable
fun BlurPanel(
    modifier: Modifier = Modifier,
    resourceId: String = "blur4.png",
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    blurRadius: Dp = 12.dp,
    alpha: Float = 0.3f,
    content: @Composable BoxScope.() -> Unit = {}
) = withDesktopScope {
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
            src = painterResource("images/$resourceId"),
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
