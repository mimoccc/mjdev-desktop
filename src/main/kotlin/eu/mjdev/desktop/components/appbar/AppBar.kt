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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    iconVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    actionsVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    iconPadding: PaddingValues = PaddingValues(),
    titlePadding: PaddingValues = PaddingValues(),
    actionsPadding: PaddingValues = PaddingValues(),
    icon: @Composable RowScope.() -> Unit = {},
    title: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .padding(contentPadding),
        verticalAlignment = Alignment.Bottom,
    ) {
        Row(
            modifier = Modifier.wrapContentHeight()
                .padding(iconPadding),
            verticalAlignment = iconVerticalAlignment,
            content = icon
        )
        Row(
            modifier = Modifier.weight(1f).wrapContentHeight()
                .padding(titlePadding),
            verticalAlignment = titleVerticalAlignment,
            content = title
        )
        Row(
            modifier = Modifier.wrapContentHeight()
                .padding(actionsPadding),
            verticalAlignment = actionsVerticalAlignment,
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
