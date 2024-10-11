/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.blur

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

// todo real blur
@Composable
fun BlurPanel(
    modifier: Modifier = Modifier,
    resourceId: String = "blur4.png",
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    blurRadius: Dp = 20.dp,
    alpha: Float = 0.5f,
    content: @Composable BoxScope.() -> Unit = {}
) = withDesktopScope {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints
    ) {
        ImageAny(
            modifier = modifier.fillMaxSize().alpha(alpha).blur(blurRadius),
            src = painterResource("images/$resourceId"),
            contentScale = if (maxHeight > maxWidth) ContentScale.FillHeight else ContentScale.FillWidth
        )
        content()
    }
}
