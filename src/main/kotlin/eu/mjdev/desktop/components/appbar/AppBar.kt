/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.appbar

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    contentPadding :PaddingValues = PaddingValues(4.dp),
    icon: @Composable RowScope.() -> Unit = {},
    title: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = icon
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            content = title
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Preview
@Composable
fun AppBarPreview() = AppBar(
    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Green),
    icon = {
        Box(
            modifier = Modifier.size(24.dp)
                .background(Color.Red)
        )
    },
    title = {
        Box(
            modifier = Modifier.height(24.dp)
                .fillMaxWidth()
                .background(Color.Blue)
        )
    },
    actions = {
        Box(
            modifier = Modifier.size(24.dp, 24.dp)
                .background(Color.Yellow)
        )
    }
)
