@file:Suppress("unused")

package org.mjdev.desktop.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.debugInspectorInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.mjdev.desktop.helpers.compose.FocusHelper

object FocusState {
    val MutableState<FocusState>.isFocused
        get() = value.isFocused || value.hasFocus

    val MutableState<FocusState>.isNotFocused
        get() = !isFocused

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    val FocusState.isFocused
        get() = this.isFocused || this.hasFocus

    val FocusState.isNotFocused
        get() = !isFocused

    @Composable
    fun rememberFocusState(
        initial: FocusState = FocusHelper(false)
    ) = remember(initial) {
        mutableStateOf(initial)
    }

    @Composable
    fun rememberFocusState(
        key: Any?,
        initial: FocusState = FocusHelper(false),
    ) = remember(key) {
        mutableStateOf(initial)
    }

    fun Modifier.focusState(
        focusState: MutableState<FocusState>
    ): Modifier = onFocusChanged { state ->
        focusState.value = state
    }

    @Composable
    fun rememberFocusRequester(
        key: Any? = Unit
    ) = remember(key) { FocusRequester() }

    fun Modifier.bringIntoViewIfChildrenAreFocused(): Modifier = composed(
        inspectorInfo = debugInspectorInfo { name = "bringIntoViewIfChildrenAreFocused" },
        factory = {
            var myRect: Rect = Rect.Zero
            this.onSizeChanged {
                myRect = Rect(Offset.Zero, Offset(it.width.toFloat(), it.height.toFloat()))
            }.bringIntoViewResponder(
                remember {
                    object : BringIntoViewResponder {
                        @ExperimentalFoundationApi
                        override fun calculateRectForParent(localRect: Rect): Rect = myRect

                        @ExperimentalFoundationApi
                        override suspend fun bringChildIntoView(localRect: () -> Rect?) {
                        }
                    }
                }
            )
        }
    )

    @Composable
    fun Modifier.requestFocusOnTouch(
        focusRequester: FocusRequester,
        requestFocus: Boolean = true,
        scope: CoroutineScope = rememberCoroutineScope(),
        onTouch: (() -> Unit)? = null
    ): Modifier = this then focusRequester(
        focusRequester
    ).pointerInput(this) {
        detectTapGestures(
            onTap = {
                try {
                    scope.launch {
                        onTouch?.invoke()
                        if (requestFocus) {
                            focusRequester.requestFocus()
                        }
                    }
                } catch (e: Throwable) {
                    // todo
//                    Log.e(e)
                }
            }
        )
    }
}