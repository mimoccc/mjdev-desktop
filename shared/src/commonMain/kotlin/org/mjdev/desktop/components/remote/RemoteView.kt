package org.mjdev.desktop.components.remote

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

// todo
@Composable
fun RemoteView(modifier: Modifier = Modifier) {
    Box(modifier) {
        Text(text = "RemoteView")
    }
}

@Preview
@Composable
private fun PreviewPreviewRemote() {
    RemoteView()
}
