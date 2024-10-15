package eu.mjdev.desktop.components.gallery

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.components.card.PhotoCard
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.components.immersivelist.ImmersiveList
import eu.mjdev.desktop.components.immersivelist.base.ImmersiveInnerList
import eu.mjdev.desktop.extensions.Compose.rememberDerivedState
import eu.mjdev.desktop.extensions.Compose.rememberFocusRequester
import eu.mjdev.desktop.helpers.bitmap.Bitmap
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    list: List<Any?> = listOf(Unit),
    index: Int = 0,
    delayedHide: Long = 5000,
    controlsState: MutableState<Boolean> = remember { mutableStateOf(true) },
    currentItemIndex: MutableIntState = remember { mutableIntStateOf(index) },
    focusRequester: FocusRequester = rememberFocusRequester(),
    imageScaleType: MutableState<ContentScale> = rememberSaveable(
        saver = contentScaleSaver
    ) {
        mutableStateOf(ContentScale.Crop)
    },
    listState: LazyListState = rememberLazyListState(),
//    switchImageScale: () -> Unit = {
//        imageScaleType.value = when (imageScaleType.value) {
//            ContentScale.Fit -> ContentScale.Crop
//            ContentScale.Crop -> ContentScale.Fit
//            else -> ContentScale.Fit
//        }
//    },
    customContentViewer: @Composable (
        src: Any?,
        type: Any?,
        list: List<Any?>
    ) -> Unit = { _, _, _ -> },
) {
    val initialized = remember(list, index) { mutableStateOf(false) }
//    val nextItem: () -> Unit = {
//        if (currentItemIndex.intValue < (list.size - 1)) {
//            currentItemIndex.intValue += 1
//        }
//        controlsState.value = true
//    }
//    val prevItem: () -> Unit = {
//        if (currentItemIndex.intValue > 0) {
//            currentItemIndex.intValue -= 1
//        }
//        controlsState.value = true
//    }
    val itemState = rememberDerivedState(0, currentItemIndex.intValue) {
        list[currentItemIndex.intValue]
    }
    val itemType = rememberItemType(itemState.value)
    val imageSrc = rememberImageFromItem(itemState.value)
    BoxWithControls(
        modifier = modifier,
        src = itemState.value,
        controlsState = controlsState,
        controls = { src, bckColor, state ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                ItemInfo(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    src = src,
                    visible = state.value,
                    backgroundColor = bckColor
                )
            }
        }
    ) { _, bckColor, _ ->
        ImmersiveList(
            modifier = Modifier.fillMaxSize(),
            currentItemIndex = currentItemIndex,
            listAlignment = Alignment.BottomStart,
            background = { _, _ ->
                ImageAny(
                    src = imageSrc.value,
                    contentDescription = null,
                    contentScale = imageScaleType.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .focusable()
                        .focusRequester(focusRequester)
//                        .swipeGestures(
//                            onTap = { state.toggle() },
//                            onDoubleTap = { switchImageScale() },
//                            onSwipeLeft = { nextItem() },
//                            onSwipeRight = { prevItem() },
//                            onSwipeUp = { nextItem() },
//                            onSwipeDown = { prevItem() },
//                        )
                )
                customContentViewer(imageSrc.value, itemType, list)
            }
        ) {
            ImmersiveInnerList(
                modifier = Modifier.fillMaxWidth(),
                visible = controlsState.value,
                backgroundColor = bckColor
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    state = listState
                ) {
                    itemsIndexed(list) { index, item ->
                        val fr = rememberFocusRequester()
                        PhotoCard(
                            modifier = Modifier.immersiveListItem(index),
                            item = item,
                            focusRequester = fr,
                        )
                        SideEffect {
                            val isFocused = (currentItemIndex.intValue == index)
                            if (isFocused) {
                                try {
                                    fr.requestFocus()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    LaunchedEffect(currentItemIndex.intValue, controlsState.value) {
        if (!initialized.value) {
            listState.scrollToItem(currentItemIndex.intValue)
            initialized.value = true
        } else {
            if (controlsState.value && (delayedHide > 0)) {
                delay(delayedHide)
                controlsState.value = false
            }
        }
    }
}

@Composable
fun rememberItemType(item: Any?): Any? = remember(item) { item }

@Composable
fun rememberImageFromItem(image: Any?): State<Any?> = remember(image) {
    derivedStateOf {
        val color = image as? Color
        val file = image as? File
        val bitmap = image as? Bitmap
        val string = image.toString()
        // todo more types & gif
        file ?: bitmap ?: color ?: string
    }
}

val contentScaleSaver = Saver<MutableState<ContentScale>, String>(
    save = { value -> value.value.toString() },
    restore = { str ->
        mutableStateOf(
            when (str) {
                "Crop" -> ContentScale.Crop
                "Fit" -> ContentScale.Fit
                else -> ContentScale.Crop
            }
        )
    }
)

@Preview
@Composable
fun GalleryPreview() = Gallery()
