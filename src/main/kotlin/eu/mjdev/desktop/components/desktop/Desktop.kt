package eu.mjdev.desktop.components.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.desktop.widgets.MemoryChart

@Suppress("FunctionName")
@Preview
@Composable
fun Desktop(
    modifier: Modifier = Modifier
) = Box(
    modifier = modifier
) {
    Box(
        modifier = modifier
    ) {
//        ComposeWebView(
//            modifier = Modifier.size(640.dp, 480.dp)
        //            .align(Alignment.Center)
//        )
        MemoryChart(
            modifier = Modifier.size(350.dp, 300.dp)
                .align(Alignment.BottomEnd)
        )
    }
}