/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.immersivelist.base

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import eu.mjdev.desktop.extensions.ColorUtils.createVerticalColorBrush
import eu.mjdev.desktop.helpers.compose.Gravity

@Immutable
class ImmersiveListScope internal constructor(
    private val onFocused: (Int) -> Unit
) {
    fun Modifier.immersiveListItem(index: Int): Modifier {
        return this then onFocusChanged {
            if (it.isFocused) onFocused(index)
        }
    }
}

@Composable
fun ImmersiveListScope.ImmersiveInnerList(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    enter: EnterTransition = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
    exit: ExitTransition = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
    content: @Composable ImmersiveListScope.() -> Unit = {},
) {
    val background: State<Brush> = remember(backgroundColor) {
        derivedStateOf {
            if (backgroundColor == Color.Transparent) {
                verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color.Transparent
                    )
                )
            } else {
                createVerticalColorBrush(
                    backgroundColor,
                    Gravity.BOTTOM
                )
            }
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = enter,
        exit = exit
    ) {
        Box(
            modifier = modifier.background(background.value)
        ) {
            content()
        }
    }
}
