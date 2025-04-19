/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.card

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.card.base.CardBorder
import org.mjdev.desktop.components.card.base.CardColors
import org.mjdev.desktop.components.card.base.CardDefaults
import org.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceBorder
import org.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceColors
import org.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceGlow
import org.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceScale
import org.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceShape
import org.mjdev.desktop.components.card.base.CardGlow
import org.mjdev.desktop.components.card.base.CardScale
import org.mjdev.desktop.components.card.base.CardShape
import org.mjdev.desktop.components.surface.Surface
import org.mjdev.desktop.components.surface.base.Glow
import org.mjdev.desktop.extensions.Compose.preview
import org.jetbrains.compose.ui.tooling.preview.Preview

// todo, card like in android tv
@Composable
fun Card(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    shape: CardShape = CardDefaults.shape(),
    colors: CardColors = CardDefaults.colors(),
    scale: CardScale = CardDefaults.scale(),
    border: CardBorder = CardDefaults.border(),
    glow: CardGlow = CardDefaults.glow(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) = Surface(
    onClick = onClick,
    onLongClick = onLongClick,
    modifier = modifier,
    shape = shape.toClickableSurfaceShape(),
    colors = colors.toClickableSurfaceColors(),
    scale = scale.toClickableSurfaceScale(),
    border = border.toClickableSurfaceBorder(),
    glow = glow.toClickableSurfaceGlow(),
    interactionSource = interactionSource,
) {
    Column(content = content)
}

@Preview
@Composable
fun CardPreview() = preview {
    Card(
        modifier = Modifier.size(200.dp, 128.dp),
        colors = CardDefaults.colors(
            containerColor = Color.White
        ),
        border = CardDefaults.border(2.dp),
        glow = CardDefaults.glow(glow = Glow(Color.Green, 4.dp))
    )
}
