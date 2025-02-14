package org.mjdev.desktop.components.chromecast

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview

// todo
@Composable
fun ChromecastView(
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        Text(text = "ChromecastView")
    }
}

//@Preview(name = "ChromecastView")
@Composable
private fun PreviewChromecastView() {
    ChromecastView()
}