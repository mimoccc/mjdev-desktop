package eu.mjdev.desktop.components.custom

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Compose.transparent
import eu.mjdev.desktop.extensions.Modifier.circleBorder
import eu.mjdev.desktop.extensions.Modifier.clipCircle
import eu.mjdev.desktop.provider.DesktopProvider
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop

@Preview
@Composable
fun UserAvatar(
    backgroundColor: Color,
    avatarSize: Dp = 128.dp,
    orientation: Orientation = Orientation.Vertical,
    api: DesktopProvider = LocalDesktop.current,
) = Box(
    modifier = Modifier
        .fillMaxWidth()
        .background(backgroundColor)
        .padding(16.dp)
) {
    when (orientation) {
        Orientation.Vertical -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier
                    .size(avatarSize)
                    .clipCircle(),
                contentPadding = PaddingValues(0.dp),
                onClick = {},
                colors = ButtonDefaults.transparent(),
                elevation = ButtonDefaults.noElevation()
            ) {
                Image(
                    modifier = Modifier
                        .size(avatarSize)
                        .circleBorder(2.dp, Color.White.copy(alpha = 0.6f)),
                    imageVector = api.currentUser.picture,
                    colorFilter = ColorFilter.tint(Color.White),
                    contentDescription = ""
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                text = api.currentUser.name ?: api.appsProvider.homeDir.name,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Orientation.Horizontal -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Start,
        ) {
            Button(
                modifier = Modifier
                    .size(avatarSize)
                    .clipCircle(),
                contentPadding = PaddingValues(0.dp),
                onClick = {},
                colors = ButtonDefaults.transparent(),
                elevation = ButtonDefaults.noElevation()
            ) {
                Image(
                    modifier = Modifier
                        .size(avatarSize)
                        .circleBorder(2.dp, Color.White.copy(alpha = 0.6f)),
                    imageVector = api.currentUser.picture,
                    colorFilter = ColorFilter.tint(Color.White),
                    contentDescription = ""
                )
            }
            Text(
                modifier = Modifier.padding(start = 16.dp).fillMaxWidth().padding(top = 16.dp),
                text = api.currentUser.name ?: api.appsProvider.homeDir.name,
                color = Color.White,
                textAlign = TextAlign.Start
            )
        }
    }
}