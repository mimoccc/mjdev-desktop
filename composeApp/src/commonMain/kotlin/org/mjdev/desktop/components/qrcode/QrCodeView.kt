package org.mjdev.desktop.components.qrcode

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrLogoShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.mjdev.desktop.extensions.Compose.preview
import org.jetbrains.compose.ui.tooling.preview.Preview

// todo : params
@Suppress("unused", "FunctionName")
@Composable
fun QrCodeView(
    modifier: Modifier = Modifier,
    data: String = "https://mjdev.org",
    logoPainter: Painter? = null,
    contentPadding: PaddingValues = PaddingValues(8.dp, 48.dp),
    contentDescription: String = data,
    framesColor: Color = Color.Black,
    pointColor: Color = Color.Black
) {
    val qrcodePainter = rememberQrCodePainter(data) {
        if (logoPainter != null) {
            logo {
                painter = logoPainter
                padding = QrLogoPadding.Natural(.1f)
                shape = QrLogoShape.circle()
                size = 0.2f
            }
        }
        shapes {
            ball = QrBallShape.circle()
            darkPixel = QrPixelShape.roundCorners()
            frame = QrFrameShape.roundCorners(.25f)
        }
        colors {
            dark = QrBrush.solid(pointColor)
            frame = QrBrush.solid(framesColor)
        }
    }
    Box(
        modifier = modifier.padding(contentPadding)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = qrcodePainter,
            contentDescription = contentDescription
        )
    }
}

@Preview
@Composable
fun PreviewQrCodeView() = preview(320, 320) {
    QrCodeView()
}
