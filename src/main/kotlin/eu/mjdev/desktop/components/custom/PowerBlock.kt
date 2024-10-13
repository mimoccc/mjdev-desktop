package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsPower
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.ColorUtils.alpha
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Compose.transparent
import eu.mjdev.desktop.extensions.Modifier.circleBorder
import eu.mjdev.desktop.extensions.Modifier.circleShadow
import eu.mjdev.desktop.extensions.Modifier.clipCircle
import eu.mjdev.desktop.extensions.Modifier.rectShadow
import eu.mjdev.desktop.helpers.shape.BarShape
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

// todo shadow? background?
@Composable
fun PowerBlock(
    backgroundColor: Color = Color.Transparent,
    shadowColor: Color = Color.Black.copy(alpha = 0.3f),
    iconTintColor: Color = Color.Black,
    bottomBoxHeight: Dp = 128.dp,
    iconHeight: Dp = 64.dp,
    onPowerButtonClick: () -> Unit = {}
) = withDesktopScope {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomBoxHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            val shape = BarShape(
                offset = (minWidth / 2).value,
                circleRadius = 24.dp,
                cornerRadius = 4.dp,
                circleGap = 8.dp,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(iconHeight)
                    .rectShadow(color = shadowColor)
                    .background(backgroundColor, shape)
            )
            Button(
                modifier = Modifier
                    .padding(bottom = 36.dp)
                    .size(64.dp)
                    .clipCircle()
                    .circleBorder(2.dp, textColor.alpha(0.5f))
                    .circleShadow(4.dp, shadowColor),
                contentPadding = PaddingValues(1.dp),
                onClick = onPowerButtonClick,
                colors = ButtonDefaults.transparent(),
                elevation = ButtonDefaults.noElevation()
            ) {
                Image(
                    modifier = Modifier
                        .size(64.dp)
                        .background(backgroundColor, CircleShape)
                        .clipCircle()
                        .padding(8.dp),
                    imageVector = Icons.Filled.SettingsPower,
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(textColor)
                )
            }
        }
    }
}

@Preview
@Composable
fun PowerBlockPreview() = PowerBlock()