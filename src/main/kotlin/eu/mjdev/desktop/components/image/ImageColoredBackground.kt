/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import eu.mjdev.desktop.extensions.BitmapUtils.cut
import eu.mjdev.desktop.extensions.BitmapUtils.topMostColor
import eu.mjdev.desktop.extensions.ColorUtils.nonAlphaValue
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Modifier.conditional
import eu.mjdev.desktop.extensions.Image.loadPicture

@Composable
fun ImageColoredBackground(
    modifier: Modifier = Modifier,
    src: Any? = null,
    shape: Shape = RectangleShape,
    defaultBackgroundColor: Color = Color.SuperDarkGray,
    transform: ((color: Color) -> Brush)? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable (bckColor: Color) -> Unit = {}
) {
    var bckColor by remember(src) { mutableStateOf(defaultBackgroundColor) }
    Box(
        modifier = modifier
            .conditional(transform == null) {
                background(bckColor, RectangleShape)
            }
            .conditional(transform != null) {
                background(
                    transform!!.invoke(bckColor),
                    shape
                )
            },
        contentAlignment = contentAlignment
    ) {
        content(bckColor)
    }
    LaunchedEffect(src) {
        runCatching {
            loadPicture(src).getOrNull()?.let { image ->
                val width = image.width
                val height = image.height
                val imagePart1 = image.cut(0, 0, 64, 64)
                val imagePart2 = image.cut(width - 64, 0, 64, 64)
                val imagePart3 = image.cut(0, height - 64, 64, 64)
                val imagePart4 = image.cut(width - 64, height - 64, 64, 64)
                val colors = listOf(
                    imagePart1.topMostColor,
                    imagePart2.topMostColor,
                    imagePart3.topMostColor,
                    imagePart4.topMostColor,
                )
                colors.toList().minBy { it.nonAlphaValue }
            }
        }.getOrNull()?.let { bc ->
            bckColor = bc
        }
    }
}