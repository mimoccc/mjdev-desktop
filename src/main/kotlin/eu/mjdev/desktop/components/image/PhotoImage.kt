/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.image

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// todo
@Composable
fun PhotoImage(
    src: Any? = null,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable () -> Unit = {
        ImageAny(
            modifier = modifier,
            src = Icons.Filled.BrokenImage,
            contentScale = contentScale
        )
    },
    alpha: Float = DefaultAlpha,
    contrast: Float = 5f,
    brightness: Float = -255f,
    borderSize: Dp = 2.dp,
    borderColor: Color = Color.Black,
    backgroundColor: Color = Color.Black,
    roundCornerSize: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(roundCornerSize),
    colorFilter: ColorFilter? = null,
    contentDescription: String? = null,
//    onImageStateChanged: ((state: GlideImageState) -> Unit)? = null
) {
    if (src == null) {
        placeholder()
    } else {
        ImageAny(
            src = src,
            modifier = modifier.border(
                BorderStroke(borderSize, borderColor),
                shape
            ).background(
                backgroundColor,
                shape
            ).alpha(alpha),
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
//            filterQuality = filterQuality,
            placeholder = placeholder,
            contentDescription = contentDescription,
            onLoading = {},
            onFail = {}
        )
//        GlideImage(
//            imageModel = { src },
//            modifier = modifier
//                .defaultMinSize(80.dp)
//                .conditional(false) {
//                    aspectRatio(16f / 9f)
//                }
//                .border(
//                    BorderStroke(borderSize, borderColor),
//                    shape
//                ),
//            imageOptions = ImageOptions(
//                contentScale = contentScale,
//                alignment = alignment,
//                contentDescription = contentDescription,
//                alpha = alpha,
//                colorFilter = colorFilter,
//            ),
//            onImageStateChanged = { state ->
//                onImageStateChanged?.invoke(state)
//            },
//            success = { state, _ ->
//                val bitmap = state.imageBitmap?.asAndroidBitmap()
//                Box(
//                    modifier = modifier.background(bitmap.majorColor(backgroundColor), shape),
//                ) {
//                    ImageAny(
//                        src = bitmap
//                            ?.toDrawable()
//                            ?.asPhoto(),
//                        modifier = modifier,
//                        alignment = alignment,
//                        contentScale = contentScale,
//                        alpha = alpha,
//                        colorFilter = contrastAndBrightness(
//                            contrast,
//                            brightness
//                        ),
//                        contentDescription = contentDescription,
//                    )
//                }
//            },
//            failure = { failure ->
//                Timber.e(failure.reason)
//                placeholder()
//            },
//        )
    }
}

@Preview
@Composable
fun PhotoImagePreview() = PhotoImage()
