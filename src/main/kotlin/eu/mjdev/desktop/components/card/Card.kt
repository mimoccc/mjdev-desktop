/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.card

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.card.base.*
import eu.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceBorder
import eu.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceColors
import eu.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceGlow
import eu.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceScale
import eu.mjdev.desktop.components.card.base.CardDefaults.toClickableSurfaceShape
import eu.mjdev.desktop.components.surface.Surface
import eu.mjdev.desktop.components.surface.base.Glow
import eu.mjdev.desktop.extensions.Compose.preview

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
