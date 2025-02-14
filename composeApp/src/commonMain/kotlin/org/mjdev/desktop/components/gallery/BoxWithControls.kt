package org.mjdev.desktop.components.gallery

//@Composable
//fun BoxWithControls(
//    modifier: Modifier = Modifier,
//    src: Any? = null,
//    contentAlignment: Alignment = Alignment.BottomCenter,
//    controlsState: MutableState<Boolean> = remember { mutableStateOf(false) },
//    controls: @Composable (
//        src: Any?,
//        bckColor: Color,
//        controlsState: MutableState<Boolean>
//    ) -> Unit = { _, _, _ -> },
//    content: @Composable (
//        src: Any?,
//        bckColor: Color,
//        controlsState: MutableState<Boolean>
//    ) -> Unit = { _, _, _ -> }
//) = ImageColoredBackground(
//    modifier = modifier.pointerInput(Unit) {
//        detectTapGestures(
//            onTap = {
//                controlsState.toggle()
//            }
//        )
//    }.onKey(Key.Enter) {
//        controlsState.toggle()
//    }.onKey(Key.DirectionDown) {
//        controlsState.value = false
//    }.onKey(Key.DirectionUp) {
//        controlsState.value = true
//    },
//    src = src,
//    contentAlignment = contentAlignment,
//) { bckColor ->
//    content(src, bckColor, controlsState)
//    if (controlsState.value) {
//        controls(src, bckColor, controlsState)
//    }
//}
//
//@Preview
//@Composable
//fun BoxWithControlsPreview() = preview {
//    BoxWithControls()
//}
//