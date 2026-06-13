package org.mjdev.desktop.components.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.extensions.ButtonDefaults.noElevation
import org.mjdev.desktop.extensions.ButtonDefaults.transparent
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.circleBorder
import org.mjdev.desktop.extensions.Modifier.clipCircle
// import org.mjdev.desktop.extensions.circleShadow
// import org.mjdev.desktop.extensions.rectShadow
import org.mjdev.desktop.helpers.shape.BarShape
import org.mjdev.desktop.icons.system.PowerOff
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PowerBlock(
    modifier: Modifier = Modifier,
    shadowColor: Color = Color.Black.copy(alpha = 0.3f),
    bottomBoxHeight: Dp = 128.dp,
    iconHeight: Dp = 64.dp,
    onPowerButtonClick: () -> Unit = {},
) = withDesktopContext {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(bottomBoxHeight),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val shape =
                BarShape(
                    offset = (minWidth / 2),
                    circleRadius = 24.dp,
                    cornerRadius = 4.dp,
                    circleGap = 8.dp,
                )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(iconHeight)
//                    .rectShadow(color = shadowColor, shape = shape)
                        .background(backgroundColor, shape),
            )
            Button(
                modifier =
                    Modifier
                        .padding(bottom = 36.dp)
                        .size(64.dp)
                        .clipCircle()
                        .circleBorder(2.dp, textColor.alpha(0.5f)),
                //                    .circleShadow(4.dp, shadowColor),
                contentPadding = PaddingValues(1.dp),
                onClick = onPowerButtonClick,
                colors = ButtonDefaults.transparent(),
                elevation = ButtonDefaults.noElevation(),
            ) {
                Image(
                    modifier =
                        Modifier
                            .size(64.dp)
                            .background(backgroundColor, CircleShape)
                            .clipCircle()
                            .padding(8.dp),
                    imageVector = PowerOff,
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(textColor),
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewPowerBlock() =
    preview {
        PowerBlock()
    }
