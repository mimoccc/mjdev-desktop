package eu.mjdev.desktop.components.controlpanel.pages

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.DateTime
import eu.mjdev.desktop.components.PowerBlock
import eu.mjdev.desktop.components.UserAvatar
import eu.mjdev.desktop.components.controlpanel.ControlCenterPage
import eu.mjdev.desktop.extensions.Compose.rectShadow

@Suppress("FunctionName")
fun MainSettingsPage() = ControlCenterPage(
    icon = Icons.Filled.Home,
    name = "Home"
) { backgroundColor ->
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .rectShadow(8.dp, Color.Black)
        ) {
            UserAvatar(
                backgroundColor = backgroundColor,
                avatarSize = 128.dp,
                orientation = Orientation.Vertical
            )
            Divider(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = Color.Transparent,
                thickness = 2.dp
            )
            DateTime(
                backgroundColor = backgroundColor
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            PowerBlock(
                backgroundColor = backgroundColor
            )
        }
    }
}
