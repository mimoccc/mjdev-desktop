/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.card

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.card.base.*
import org.mjdev.desktop.components.image.ImageAny
import org.mjdev.desktop.components.surface.base.Glow
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.user.AccountCircle

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

//@Preview
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
                src = AccountCircle,
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
