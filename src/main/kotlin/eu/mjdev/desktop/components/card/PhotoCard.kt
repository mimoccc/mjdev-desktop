package eu.mjdev.desktop.components.card

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.card.base.CardDefaults
import eu.mjdev.desktop.components.card.base.CardScale
import eu.mjdev.desktop.extensions.Compose.rememberFocusState
import eu.mjdev.desktop.extensions.Compose.rememberFocusRequester
import eu.mjdev.desktop.helpers.compose.FocusHelper
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.components.image.PhotoImage
import eu.mjdev.desktop.extensions.Compose.isFocused
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

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
) {
    ItemCard(
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
}

@Composable
fun computeCardWidth(
    api: DesktopProvider = LocalDesktop.current,
    ratio: Float = 2.5f
): Dp =
//    LocalConfiguration.current.let { config ->
//    if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
    api.containerSize.width / ratio
//    else
//        config.screenHeightDp / ratio
//}.dp

