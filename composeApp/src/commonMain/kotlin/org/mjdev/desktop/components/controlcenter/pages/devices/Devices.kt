package org.mjdev.desktop.components.controlcenter.pages.devices

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.mobile.MobileFriendly

@Suppress("FunctionName")
fun DevicesPage(context: IDesktopContext) =
    ControlCenterPage(
        context = context,
        icon = MobileFriendly,
        name = "Connected devices",
        condition = {
            false
//        connectionManager.hasConnectedDevices
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
        }
    }

@Preview
@Composable
fun DevicesPagePreview() =
    preview {
        DevicesPage(context).Render()
    }
