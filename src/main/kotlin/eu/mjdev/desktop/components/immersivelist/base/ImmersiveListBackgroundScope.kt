/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.immersivelist.base

import androidx.compose.animation.*
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Immutable
class ImmersiveListBackgroundScope(
    boxScope: BoxScope
) : BoxScope by boxScope {

    @Suppress("unused")
    @Composable
    fun AnimatedVisibility(
        visible: Boolean,
        modifier: Modifier = Modifier,
        enter: EnterTransition = ImmersiveListDefaults.EnterTransition,
        exit: ExitTransition = ImmersiveListDefaults.ExitTransition,
        label: String = "AnimatedVisibility",
        content: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible,
            modifier,
            enter,
            exit,
            label,
            content
        )
    }

    @Suppress("unused")
    @Composable
    fun AnimatedContent(
        targetState: Int,
        modifier: Modifier = Modifier,
        transitionSpec: AnimatedContentTransitionScope<Int>.() -> ContentTransform = {
            ImmersiveListDefaults.EnterTransition.togetherWith(ImmersiveListDefaults.ExitTransition)
        },
        contentAlignment: Alignment = Alignment.TopStart,
        label: String = "AnimatedContent",
        content: @Composable AnimatedVisibilityScope.(targetState: Int) -> Unit
    ) {
        androidx.compose.animation.AnimatedContent(
            targetState,
            modifier,
            transitionSpec,
            contentAlignment,
            content = content,
            label = label
        )
    }
}