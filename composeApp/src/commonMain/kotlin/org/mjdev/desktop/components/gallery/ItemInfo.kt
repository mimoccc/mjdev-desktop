package org.mjdev.desktop.components.gallery

//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.desktop.ui.tooling.preview.Preview
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.derivedStateOf
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import org.mjdev.desktop.components.text.AutoHideEmptyText
//import org.mjdev.desktop.data.ItemWithTitle
//import org.mjdev.desktop.extensions.Colors.createVerticalColorBrush
//import org.mjdev.desktop.extensions.Colors.invert
//import org.mjdev.desktop.extensions.Compose.preview
//import org.mjdev.desktop.helpers.compose.Gravity
//
//// todo
//@Composable
//fun ItemInfo(
//    modifier: Modifier = Modifier,
//    src: Any? = null,
//    visible: Boolean = true,
//    backgroundColor: Color = Color.Transparent,
////    metadataRetriever: MetadataRetriever = rememberMetaDataRetriever(),
//    titleFromItem: () -> Any? = {
//        (src as? ItemWithTitle)?.title
//    },
//    dateFromItem: () -> Any? = {
//        (src as? ItemWithDate)?.date?.toString() ?: "-"
//    },
//    detailsFromItem: () -> Any? = {
////        metadataRetriever.getInfo(src)
//    },
//) {
//    val bck = createVerticalColorBrush(backgroundColor, Gravity.TOP)
//    val textColor by remember(backgroundColor) {
//        derivedStateOf {
//            backgroundColor.invert().copy(alpha = 1.0f)
//        }
//    }
//    AnimatedVisibility(
//        modifier = modifier,
//        visible = visible,
//        enter = slideInVertically(),
//        exit = slideOutVertically(),
//    ) {
//        Box(
//            modifier = modifier.background(bck),
//            contentAlignment = Alignment.TopStart
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 16.dp)
//            ) {
//                Spacer(modifier = Modifier.height(16.dp))
//                AutoHideEmptyText(
//                    text = titleFromItem(),
//                    color = textColor,
//                    fontSize = 24.sp
//                )
//                AutoHideEmptyText(
//                    text = dateFromItem(),
//                    color = textColor,
//                    fontSize = 16.sp
//                )
//                AutoHideEmptyText(
//                    text = detailsFromItem(),
//                    color = textColor,
//                    fontSize = 12.sp,
//                    lineHeight = 12.sp
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }
//    }
//}
//
//@Preview
//@Composable
//fun ItemInfoPreview() = preview {
//    ItemInfo()
//}
