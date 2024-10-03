/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.card

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import eu.mjdev.desktop.components.card.base.*
import eu.mjdev.desktop.components.card.base.CardDefaults.DescriptionAlpha
import eu.mjdev.desktop.components.card.base.CardDefaults.SubtitleAlpha

@Composable
fun CompactCard(
    onClick: () -> Unit,
    image: @Composable BoxScope.() -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    subtitle: @Composable () -> Unit = {},
    description: @Composable () -> Unit = {},
    shape: CardShape = CardDefaults.shape(),
    colors: CardColors = CardDefaults.compactCardColors(),
    scale: CardScale = CardDefaults.scale(),
    border: CardBorder = CardDefaults.border(),
    glow: CardGlow = CardDefaults.glow(),
    scrimBrush: Brush = CardDefaults.ScrimBrush,
    interactionSource: MutableInteractionSource? = null
) {
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        scale = scale,
        border = border,
        glow = glow
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            Box(
                modifier =
                    Modifier.drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush = scrimBrush)
                        }
                    },
                contentAlignment = CardDefaults.ContentImageAlignment,
                content = image
            )
            Column { CardContent(title = title, subtitle = subtitle, description = description) }
        }
    }
}

@Composable
fun CardContent(
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit = {},
    description: @Composable () -> Unit = {}
) {
    ProvideTextStyle(MaterialTheme.typography.titleMedium) { title.invoke() }
    ProvideTextStyle(MaterialTheme.typography.bodySmall) {
        Box(Modifier.graphicsLayer { alpha = SubtitleAlpha }) { subtitle.invoke() }
    }
    ProvideTextStyle(MaterialTheme.typography.bodySmall) {
        Box(Modifier.graphicsLayer { alpha = DescriptionAlpha }) { description.invoke() }
    }
}
