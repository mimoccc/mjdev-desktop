package org.mjdev.desktop.components.card

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.card.base.CardDefaults
import org.mjdev.desktop.components.card.base.CardDefaults.HorizontalImageAspectRatio
import org.mjdev.desktop.components.card.base.CardScale
import org.mjdev.desktop.components.image.ImageAny
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.FocusState.isFocused
import org.mjdev.desktop.extensions.FocusState.rememberFocusRequester
import org.mjdev.desktop.extensions.FocusState.rememberFocusState
import org.mjdev.desktop.helpers.compose.FocusHelper
import org.mjdev.desktop.components.image.PhotoImage
import org.mjdev.desktop.interfaces.IDesktopContext

@Composable
fun PhotoCard(
    item: Any? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    textColor: Color = Color.White,
    focused: Boolean = false,
    focusState: MutableState<FocusState> = rememberFocusState(
        item,
        FocusHelper(focused)
    ),
    focusRequester: FocusRequester = rememberFocusRequester(item),
    contrast: Float = 5f,
    brightness: Float = -255f,
    placeholder: @Composable () -> Unit = {
        ImageAny(
            modifier = Modifier.fillMaxSize().padding(64.dp),
            src = null,
            contentScale = contentScale
        )
    },
    imageRenderer: @Composable () -> Unit = {
        PhotoImage(
            modifier = Modifier.fillMaxSize(),
            src = item,
            contentScale = contentScale,
            contrast = contrast,
            brightness = brightness,
            placeholder = placeholder,
            borderColor = if (focusState.isFocused) Color.Green else Color.Black,
            contentDescription = item?.toString(), // todo
        )
    },
    showTitle: Boolean = true,
    cardWidth: Dp = computeCardWidth(),
    aspectRatio: Float = 16f / 9f,
    scale: CardScale = CardDefaults.scale(),
    titlePadding: PaddingValues = PaddingValues(8.dp),
    onFocus: ((item: Any?, fromUser: Boolean) -> Unit)? = null,
    onClick: ((item: Any?) -> Unit)? = null,
) = ItemCard(
    item = item,
    modifier = modifier,
    contentScale = contentScale,
    textColor = textColor,
    focusState = focusState,
    focusRequester = focusRequester,
    focused = focused,
    aspectRatio = aspectRatio,
    cardWidth = cardWidth,
    scale = scale,
    imageRenderer = imageRenderer,
    titlePadding = titlePadding,
    onFocus = onFocus,
    onClick = onClick,
    showTitle = showTitle
)

@Composable
fun computeCardWidth(
    api: IDesktopContext = LocalDesktopContext.current,
    ratio: Float = 2.5f
): Dp {
    return if (api.containerSize.let { it.height > it.width }) api.containerSize.width / ratio
    else api.containerSize.height / ratio
}

//@Preview
@Suppress("unused")
@Composable
fun PhotoCardPreview() = preview {
    PhotoCard(
        item = "test",
        modifier = Modifier.size(200.dp, 128.dp),
        scale = CardDefaults.scale(1f),
        contentScale = ContentScale.Crop,
        textColor = Color.Black,
        showTitle = true,
        focused = true,
        aspectRatio = HorizontalImageAspectRatio,
    )
}
