package eu.mjdev.desktop.components.controlcenter.pages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.mjdev.desktop.components.controlcenter.base.ControlCenterPage

@Suppress("FunctionName")
fun DevicesPage() = ControlCenterPage(
    icon = Icons.Filled.MobileFriendly,
    name = "Connected devices"
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}

@Preview
@Composable
fun DevicesPagePreview() =  DevicesPage().render()
