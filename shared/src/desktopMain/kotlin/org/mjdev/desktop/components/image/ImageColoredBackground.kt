/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.image

// import androidx.compose.desktop.ui.tooling.preview.Preview
// import androidx.compose.foundation.background
// import androidx.compose.foundation.layout.Box
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.setValue
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Brush
// import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.RectangleShape
// import androidx.compose.ui.graphics.Shape
// import org.mjdev.desktop.extensions.BitmapUtils.toBitmap
// import org.mjdev.desktop.extensions.BitmapUtils.topMostColor
// import org.mjdev.desktop.extensions.Colors.SuperDarkGray
// import org.mjdev.desktop.extensions.Colors.nonAlphaValue
// import org.mjdev.desktop.extensions.Compose.preview
// import org.mjdev.desktop.extensions.LaunchedEffect.launchedEffect
// import org.mjdev.desktop.extensions.Modifier.conditional
// import org.mjdev.desktop.context.DesktopContext.Companion.withDesktopContext
//
// @Composable
// fun ImageColoredBackground(
//    modifier: Modifier = Modifier,
//    src: Any? = null,
//    shape: Shape = RectangleShape,
//    defaultBackgroundColor: Color = Color.SuperDarkGray,
//    transform: ((color: Color) -> Brush)? = null,
//    contentAlignment: Alignment = Alignment.TopStart,
//    content: @Composable (bckColor: Color) -> Unit = {}
// ) = withDesktopContext {
//    var bckColor by remember(src) { mutableStateOf(defaultBackgroundColor) }
//    Box(
//        modifier = modifier
//            .conditional(transform == null) {
//                background(bckColor, RectangleShape)
//            }
//            .conditional(transform != null) {
//                background(
//                    transform!!.invoke(bckColor),
//                    shape
//                )
//            },
//        contentAlignment = contentAlignment
//    ) {
//        content(bckColor)
//    }
//    launchedEffect(context) { context ->
//        runCatching {
//            context.loadPicture(src)?.let { image ->
//                val width = image.width
//                val height = image.height
//                val imagePart1 = image.toBitmap().cut(0, 0, 64, 64)
//                val imagePart2 = image.toBitmap().cut(width - 64, 0, 64, 64)
//                val imagePart3 = image.toBitmap().cut(0, height - 64, 64, 64)
//                val imagePart4 = image.toBitmap().cut(width - 64, height - 64, 64, 64)
//                val colors = listOf(
//                    imagePart1.topMostColor,
//                    imagePart2.topMostColor,
//                    imagePart3.topMostColor,
//                    imagePart4.topMostColor,
//                )
//                colors.toList().minBy { it.nonAlphaValue }
//            }
//        }.onFailure { error ->
//            error.printStackTrace()
//        }.getOrNull()?.let { bc ->
//            bckColor = bc
//        }
//    }
// }
//
// // todo
// @Suppress("unused")
// @Preview
// @Composable
// fun PreviewImageColoredBackground() = preview {
//    ImageColoredBackground()
// }
