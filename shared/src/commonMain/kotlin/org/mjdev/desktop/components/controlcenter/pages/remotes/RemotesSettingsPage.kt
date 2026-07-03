package org.mjdev.desktop.components.controlcenter.pages.remotes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.settings.SettingsSection
import org.mjdev.desktop.components.controlcenter.settings.SettingsSwitchRow
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.Colors.alpha
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.custom.Mjdev

/**
 * Control-center page for the built-in remote-desktop (VNC/RFB) server: a switch to start/stop
 * it and the list of `ip:port` addresses a viewer can connect to. The server lives in
 * [org.mjdev.desktop.managers.remote.IRemoteDesktopManager] — its desktop implementation
 * captures the screen and injects input via AWT Robot, so it works in plain runDesktop.
 */
@Suppress("FunctionName")
fun RemotesSettingsPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Mjdev, // todo dedicated icon
    name = "Remote",
    condition = { true },
) {
    val remote = context.remoteDesktop
    val running by remote.runningState

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        SettingsSection(title = "Remote desktop (VNC)") {
            SettingsSwitchRow(
                label = "Allow remote connections",
                checked = running,
                onCheckedChange = { runAsync { remote.toggle() } },
            )
            if (running) {
                TextAny(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "Connect a VNC viewer to:",
                    color = textColor,
                    fontSize = 13.sp,
                )
                val addresses = remote.addresses
                if (addresses.isEmpty()) {
                    TextAny(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "port ${remote.port} (no network address found)",
                        color = textColor.alpha(0.6f),
                        fontSize = 13.sp,
                    )
                } else {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        addresses.forEach { address ->
                            TextAny(
                                text = address,
                                color = textColor.alpha(0.8f),
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RemotesSettingsPagePreview() = preview {
    RemotesSettingsPage(context).Render()
}
