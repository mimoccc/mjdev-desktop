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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.card.base.*
import eu.mjdev.desktop.components.card.base.CardDefaults.colorFocusBorder
import eu.mjdev.desktop.components.card.base.CardDefaults.colorFocusGlow
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.components.text.AutoHideEmptyText
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.Compose.focusState
import eu.mjdev.desktop.extensions.Compose.isFocused
import eu.mjdev.desktop.extensions.Compose.rememberFocusRequester
import eu.mjdev.desktop.extensions.Compose.rememberFocusState
import eu.mjdev.desktop.extensions.Compose.requestFocusOnTouch
import eu.mjdev.desktop.helpers.compose.FocusHelper

@Composable
fun FocusableCard(
    item: Any? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    scale: CardScale = CardDefaults.scale(),
    shape: CardShape = CardDefaults.shape(),
    textColor: Color = Color.White,
    textBackgroundUnselected: Color = Color.DarkGray.copy(alpha = 0.5f),
    textBackgroundSelected: Color = Color.Green.copy(alpha = 0.5f),
    colors: CardColors = CardDefaults.colors(),
    border: CardBorder = CardDefaults.colorFocusBorder(Color.Green),
    glow: CardGlow = CardDefaults.colorFocusGlow(Color.Green),
    showTitle: Boolean = true,
    placeholder: @Composable () -> Unit = {
        Image(
            imageVector = Icons.Filled.BrokenImage,
            contentDescription = "",
            contentScale = contentScale
        )
    },
    imageRenderer: @Composable () -> Unit = {
        ImageAny(
            modifier = Modifier.fillMaxSize(),
            src = item,
            contentDescription = item?.toString(),
            contentScale = contentScale,
            placeholder = placeholder
        )
    },
    focused: Boolean = false,
    focusRequester: FocusRequester = rememberFocusRequester(item),
    focusState: MutableState<FocusState> = rememberFocusState(
        item,
        FocusHelper(focused)
    ),
    onFocus: ((item: Any?, fromUser: Boolean) -> Unit)? = null,
    onFocusChange: (state: FocusState) -> Unit = { state ->
        if (state.isFocused || state.hasFocus) {
            // todo focused from user ?
            onFocus?.invoke(item, false)
        }
    },
    titlePadding: PaddingValues = PaddingValues(8.dp),
    cardWidth: Dp = computeCardWidth(),
    aspectRatio: Float = 16f / 9f,
    onClick: ((item: Any?) -> Unit)? = null,
) = CompactCard(
    scale = scale,
    shape = shape,
    colors = colors,
    border = border,
    glow = glow,
    modifier = modifier
        .size(
            width = cardWidth,
            height = cardWidth / aspectRatio
        )
        .focusState(focusState)
        .onFocusChanged { state ->
            onFocusChange(state)
        }
        .requestFocusOnTouch(focusRequester) {
            if (focusState.isFocused) onClick?.invoke(item)
        },
    image = {
        imageRenderer()
    },
    title = {
        if (showTitle) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (focusState.isFocused) textBackgroundSelected
                        else textBackgroundUnselected,
                        RectangleShape
                    )
            ) {
                TextAny(
                    modifier = Modifier.padding(titlePadding),
                    singleLine = true,
                    color = textColor,
                    style = MaterialTheme.typography.body1,
                    text = item?.toString().orEmpty()
                )
            }
        }
    },
    subtitle = {
        if (showTitle) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (focusState.isFocused)
                            textBackgroundSelected
                        else
                            textBackgroundUnselected,
                        RectangleShape
                    )
            ) {
                AutoHideEmptyText(
                    modifier = Modifier.padding(titlePadding),
                    singleLine = true,
                    color = textColor,
                    style = MaterialTheme.typography.body2,
                    text = "" // todo
                )
            }
        }
    },
    description = {
        // todo
    },
    onClick = {
        onClick?.invoke(item)
    },
)

@Preview
@Composable
fun FocusableCardPreview() = FocusableCard()
