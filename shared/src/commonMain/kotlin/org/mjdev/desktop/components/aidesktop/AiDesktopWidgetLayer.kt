package org.mjdev.desktop.components.aidesktop

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.components.desktop.widgets.MemoryChart
import org.mjdev.desktop.components.draggable.DraggableView
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

@Composable
fun BoxScope.AiDesktopWidgetLayer(
    modifier: Modifier = Modifier,
) = withDesktopContext {
    Column(
        modifier =
            modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = AiDesktopMetrics.RightDockWidth + 40.dp,
                    bottom = AiDesktopMetrics.DockHeight + 40.dp,
                ),
    ) {
        DraggableView(
            backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
            dragBackgroundColor = androidx.compose.ui.graphics.Color.Transparent,
        ) {
            MemoryChart(
                modifier =
                    Modifier.size(
                        AiDesktopMetrics.WidgetWidth,
                        AiDesktopMetrics.WidgetHeight,
                    ),
            )
        }
    }
}
