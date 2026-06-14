/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.card

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.card.base.CardBorder
import org.mjdev.desktop.components.card.base.CardColors
import org.mjdev.desktop.components.card.base.CardDefaults
import org.mjdev.desktop.components.card.base.CardGlow
import org.mjdev.desktop.components.card.base.CardScale
import org.mjdev.desktop.components.card.base.CardShape
import org.mjdev.desktop.components.image.ImageAny
import org.mjdev.desktop.components.surface.base.Glow
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.user.AccountCircle

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
    interactionSource: MutableInteractionSource? = null,
) = Card(
    onClick = onClick,
    onLongClick = onLongClick,
    modifier = modifier,
    interactionSource = interactionSource,
    shape = shape,
    colors = colors,
    scale = scale,
    border = border,
    glow = glow,
) {
    Box(
        contentAlignment = Alignment.BottomStart,
    ) {
        Box(
            modifier =
                Modifier.drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush = scrimBrush)
                    }
                },
            contentAlignment = CardDefaults.ContentImageAlignment,
            content = image,
        )
        Column {
            CardContent(
                title = title,
                subtitle = subtitle,
                description = description,
            )
        }
    }
}

@Preview
@Composable
fun CompactCardPreview() =
    preview {
        CompactCard(
            modifier = Modifier.size(200.dp, 128.dp),
            colors =
                CardDefaults.colors(
                    containerColor = Color.White,
                ),
            border = CardDefaults.border(),
            glow = CardDefaults.glow(glow = Glow(Color.Green, 4.dp)),
            scale = CardDefaults.scale(1f),
            scrimBrush = CardDefaults.ScrimBrush,
            image = {
                ImageAny(
                    modifier = Modifier.fillMaxSize(),
                    src = AccountCircle,
                    contentDescription = "",
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
            },
        )
    }
