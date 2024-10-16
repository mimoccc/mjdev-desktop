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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.card.base.*
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.components.surface.base.Glow
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.icons.Icons

@Suppress("unused")
@Composable
fun WideClassicCard(
    onClick: () -> Unit = {},
    image: @Composable BoxScope.() -> Unit = {},
    title: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    subtitle: @Composable () -> Unit = {},
    description: @Composable () -> Unit = {},
    shape: CardShape = CardDefaults.shape(),
    colors: CardColors = CardDefaults.colors(),
    scale: CardScale = CardDefaults.scale(),
    border: CardBorder = CardDefaults.border(),
    glow: CardGlow = CardDefaults.glow(),
    contentPadding: PaddingValues = PaddingValues(),
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
    Row(modifier = Modifier.padding(contentPadding)) {
        Box(contentAlignment = CardDefaults.ContentImageAlignment, content = image)
        Column { CardContent(title = title, subtitle = subtitle, description = description) }
    }
}

@Preview
@Composable
fun WideClassicCardPreview() = preview {
    WideClassicCard(
        colors = CardDefaults.colors(
            containerColor = Color.White
        ),
        border = CardDefaults.border(),
        glow = CardDefaults.glow(glow = Glow(Color.Green, 4.dp)),
        contentPadding = PaddingValues(8.dp),
        scale = CardDefaults.scale(1f),
        image = {
            ImageAny(
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
