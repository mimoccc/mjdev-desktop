package org.mjdev.desktop.components.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import org.mjdev.desktop.components.card.base.CardDefaults.DescriptionAlpha
import org.mjdev.desktop.components.card.base.CardDefaults.SubtitleAlpha

@Composable
fun CardContent(
    title: @Composable () -> Unit = {},
    subtitle: @Composable () -> Unit = {},
    description: @Composable () -> Unit = {}
) = Column {
    ProvideTextStyle(MaterialTheme.typography.subtitle2) {
        title()
    }
    ProvideTextStyle(MaterialTheme.typography.body2) {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = SubtitleAlpha
            }
        ) {
            subtitle()
        }
    }
    ProvideTextStyle(MaterialTheme.typography.body2) {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = DescriptionAlpha
            }
        ) {
            description()
        }
    }
}
