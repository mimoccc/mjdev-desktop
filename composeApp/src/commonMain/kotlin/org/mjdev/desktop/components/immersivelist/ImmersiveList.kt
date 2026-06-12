package org.mjdev.desktop.components.immersivelist

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import org.mjdev.desktop.components.immersivelist.base.ImmersiveListBackgroundScope
import org.mjdev.desktop.components.immersivelist.base.ImmersiveListScope
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.FocusState.bringIntoViewIfChildrenAreFocused
import org.jetbrains.compose.ui.tooling.preview.Preview

@Suppress("IllegalExperimentalApiUsage")
@Composable
fun ImmersiveList(
    background: @Composable ImmersiveListBackgroundScope.(
        index: Int,
        listHasFocus: Boolean
    ) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    listAlignment: Alignment = Alignment.BottomEnd,
    currentItemIndex: MutableIntState = remember { mutableIntStateOf(0) },
    listHasFocus: MutableState<Boolean> = remember { mutableStateOf(false) },
    list: @Composable ImmersiveListScope.() -> Unit = {},
) = Box(
    modifier.bringIntoViewIfChildrenAreFocused()
) {
    ImmersiveListBackgroundScope(
        this
    ).background(
        currentItemIndex.intValue,
        listHasFocus.value
    )
    val focusManager = LocalFocusManager.current
    Box(
        Modifier
            .align(listAlignment)
            .onFocusChanged { focusState ->
                listHasFocus.value = focusState.hasFocus
            }
    ) {
        ImmersiveListScope { idx ->
            currentItemIndex.intValue = idx
            focusManager.moveFocus(FocusDirection.Enter)
        }.list()
    }
}

// todo
@Preview
@Composable
fun PreviewImmersiveList() = preview {
    ImmersiveList()
}
