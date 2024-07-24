package eu.mjdev.desktop.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.circleBorder
import eu.mjdev.desktop.extensions.Compose.circleShadow
import eu.mjdev.desktop.extensions.Compose.clipCircle
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Compose.rectShadow
import eu.mjdev.desktop.extensions.Compose.transparent

@Preview
@Composable
fun PowerBlock(
    backgroundColor: Color,
    shadowColor: Color = Color.Black,
    bottomBoxHeight: Dp = 128.dp,
    iconHeight: Dp = 64.dp
) = Column(
    modifier = Modifier.fillMaxWidth()
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bottomBoxHeight),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(iconHeight)
                .rectShadow(color = shadowColor)
                .background(backgroundColor)
        )
        Button(
            modifier = Modifier
                .padding(bottom = 36.dp)
                .size(64.dp)
                .clipCircle()
                .circleBorder(4.dp, backgroundColor)
                .circleShadow(color = shadowColor),
            contentPadding = PaddingValues(1.dp),
            onClick = {},
            colors = ButtonDefaults.transparent(),
            elevation = ButtonDefaults.noElevation()
        ) {
            Image(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White, CircleShape)
                    .clipCircle()
                    .padding(8.dp),
                imageVector = Icons.Filled.SettingsPower,
                contentDescription = "",
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
    }
}