package org.mjdev.desktop.components.chromecast

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

// todo
@Composable
fun ChromecastView(
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        Text(text = "ChromecastView")
    }
}

@Preview
@Composable
private fun PreviewChromecastView() {
    ChromecastView()
}