package eu.mjdev.desktop.components.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.text.AutoHideEmptyText
import eu.mjdev.desktop.extensions.ColorUtils.createVerticalColorBrush
import eu.mjdev.desktop.extensions.ColorUtils.invert
import eu.mjdev.desktop.helpers.compose.Gravity

// todo
@Suppress("UNUSED_PARAMETER")
@Composable
fun ItemInfo(
    modifier: Modifier = Modifier,
    src: Any? = null,
    visible: Boolean = true,
    backgroundColor: Color = Color.Transparent,
//    metadataRetriever: MetadataRetriever = rememberMetaDataRetriever(),
    titleFromItem: () -> Any? = {
//        (src as? ItemWithTitle<*>)?.title
    },
    dateFromItem: () -> Any? = {
//        (src as? ItemWithDate)?.date ?: "-"
    },
    detailsFromItem: () -> Any? = {
//        metadataRetriever.getInfo(src)
    },
) {
    val bck = createVerticalColorBrush(backgroundColor, Gravity.TOP)
    val textColor by remember(backgroundColor) {
        derivedStateOf {
            backgroundColor.invert().copy(alpha = 1.0f)
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically(),
        exit = slideOutVertically(),
    ) {
        Box(
            modifier = modifier.background(bck),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                AutoHideEmptyText(
                    text = titleFromItem(),
                    color = textColor,
                    fontSize = 24.sp
                )
                AutoHideEmptyText(
                    text = dateFromItem(),
                    color = textColor,
                    fontSize = 16.sp
                )
                AutoHideEmptyText(
                    text = detailsFromItem(),
                    color = textColor,
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}