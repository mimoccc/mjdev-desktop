/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.card

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.card.base.*
import eu.mjdev.desktop.components.card.base.CardDefaults.HorizontalImageAspectRatio
import eu.mjdev.desktop.components.card.base.CardDefaults.colorFocusBorder
import eu.mjdev.desktop.components.card.base.CardDefaults.colorFocusGlow
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.components.surface.base.Glow
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Compose.rememberFocusRequester
import eu.mjdev.desktop.extensions.Compose.rememberFocusState
import eu.mjdev.desktop.helpers.compose.FocusHelper

@Composable
fun ItemCard(
    item: Any? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    scale: CardScale = CardDefaults.scale(),
    shape: CardShape = CardDefaults.shape(),
    textColor: Color = Color.White,
    colors: CardColors = CardDefaults.colors(),
    border: CardBorder = CardDefaults.colorFocusBorder(Color.Green),
    glow: CardGlow = CardDefaults.colorFocusGlow(Color.Green),
    aspectRatio: Float = 16f / 9f,
    placeholder: @Composable () -> Unit = {
        Image(
            imageVector = Icons.Filled.BrokenImage,
            "",
            modifier
        )
    },
    imageRenderer: @Composable () -> Unit = {
        ImageAny(
            modifier = modifier.fillMaxSize(),
            src = item,
            contentDescription = item?.toString(),
            contentScale = contentScale,
//            placeholder = placeholder
        )
    },
    cardWidth: Dp = computeCardWidth(),
    focused: Boolean = false,
    focusState: MutableState<FocusState> = rememberFocusState(
        item,
        FocusHelper(focused)
    ),
    focusRequester: FocusRequester = rememberFocusRequester(item),
    titlePadding: PaddingValues = PaddingValues(8.dp),
    showTitle: Boolean = true,
    onFocus: ((item: Any?, fromUser: Boolean) -> Unit)? = null,
    onClick: ((item: Any?) -> Unit)? = null,
) = FocusableCard(
    modifier = modifier,
    item = item,
    focusState = focusState,
    focusRequester = focusRequester,
    focused = focused,
    contentScale = contentScale,
    aspectRatio = aspectRatio,
    textColor = textColor,
    scale = scale,
    shape = shape,
    colors = colors,
    border = border,
    glow = glow,
    cardWidth = cardWidth,
    imageRenderer = imageRenderer,
    titlePadding = titlePadding,
    onFocus = onFocus,
    onClick = onClick,
    showTitle = showTitle,
    placeholder = placeholder
)

@Preview
@Composable
fun ItemCardPreview() = preview {
    ItemCard(
        item = "test",
        modifier = Modifier.size(200.dp, 128.dp),
        colors = CardDefaults.colors(
            containerColor = Color.White
        ),
        border = CardDefaults.border(),
        glow = CardDefaults.glow(glow = Glow(Color.Green, 4.dp)),
        scale = CardDefaults.scale(1f),
        contentScale = ContentScale.Crop,
        textColor = Color.Black,
        showTitle = true,
        focused = true,
        aspectRatio = HorizontalImageAspectRatio,
    )
}
