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
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.card.base.*
import eu.mjdev.desktop.components.card.base.CardDefaults.DescriptionAlpha
import eu.mjdev.desktop.components.card.base.CardDefaults.SubtitleAlpha
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.components.surface.base.Glow
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.icons.Icons

// todo
@Composable
fun CompactCard(
    onClick: () -> Unit = {},
    image: @Composable BoxScope.() -> Unit = {},
    title: @Composable () -> Unit = {},
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
) = Card(
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
    Box(
        contentAlignment = Alignment.BottomStart
    ) {
        Box(
            modifier = Modifier.drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(brush = scrimBrush)
                }
            },
            contentAlignment = CardDefaults.ContentImageAlignment,
            content = image
        )
        Column {
            CardContent(
                title = title,
                subtitle = subtitle,
                description = description
            )
        }
    }
}

@Composable
fun CardContent(
    title: @Composable () -> Unit = {},
    subtitle: @Composable () -> Unit = {},
    description: @Composable () -> Unit = {}
) = Column {
    ProvideTextStyle(MaterialTheme.typography.subtitle2) {
        title()
    }
    ProvideTextStyle(MaterialTheme.typography.body2) {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = SubtitleAlpha
            }
        ) {
            subtitle()
        }
    }
    ProvideTextStyle(MaterialTheme.typography.body2) {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = DescriptionAlpha
            }
        ) {
            description()
        }
    }
}

@Preview
@Composable
fun CardContentPreview() = preview {
    CardContent(
        title = {
            TextAny("title")
        },
        subtitle = {
            TextAny("subtitle")
        },
        description = {
            TextAny("description")
        }
    )
}

@Preview
@Composable
fun CompactCardPreview() = preview {
    CompactCard(
        modifier = Modifier.size(200.dp, 128.dp),
        colors = CardDefaults.colors(
            containerColor = Color.White
        ),
        border = CardDefaults.border(),
        glow = CardDefaults.glow(glow = Glow(Color.Green, 4.dp)),
        scale = CardDefaults.scale(1f),
        scrimBrush = CardDefaults.ScrimBrush,
        image = {
            ImageAny(
                modifier = Modifier.fillMaxSize(),
                src = Icons.User,
                contentDescription = ""
            )
        },
        title = {
            TextAny("title")
        },
        subtitle = {
            TextAny("subtitle")
        },
        description = {
            TextAny("description")
        }
    )
}
