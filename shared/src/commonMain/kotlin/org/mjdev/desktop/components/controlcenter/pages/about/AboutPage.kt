package org.mjdev.desktop.components.controlcenter.pages.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.custom.Mjdev

@Suppress("FunctionName")
fun AboutPage(context: IDesktopContext) =
    ControlCenterPage(
        context = context,
        icon = Mjdev,
        name = "About",
        condition = { true },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        )
    }

@Preview
@Composable
fun PreviewAboutPage() =
    preview {
        AboutPage(context).Render()
    }
