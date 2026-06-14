/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.blur

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.text.TextAny

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlurPanelHaze(
    modifier: Modifier = Modifier,
    hazeState: HazeState = rememberHazeState(),
) {
    Box {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
        ) {
            // todo
        }
        LargeTopAppBar(
            // Need to make app bar transparent to see the content behind
            colors = TopAppBarDefaults.largeTopAppBarColors(Color.Transparent),
            title = {
                TextAny("Test Blur Panel Haze")
            },
            modifier =
                Modifier
                    // We use hazeEffect on anything where we want the background
                    // blurred.
                    .hazeEffect(
                        state = hazeState,
//                    style = HazeMaterials.ultraThin()
                    ).fillMaxWidth(),
        )
    }
}

@Preview()
@Composable
private fun PreviewBlurPanelHaze() {
    BlurPanelHaze()
}
