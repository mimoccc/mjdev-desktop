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
import eu.mjdev.desktop.extensions.Compose.preview
import eu.mjdev.desktop.extensions.Modifier.conditional
import eu.mjdev.desktop.helpers.compose.Orientation

// todo alignment scopes
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    contentAlignment: Alignment.Vertical = Alignment.CenterVertically,
    iconVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    actionsVerticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    titleHorizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    iconHorizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    actionsHorizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    iconPadding: PaddingValues = PaddingValues(),
    titlePadding: PaddingValues = PaddingValues(),
    actionsPadding: PaddingValues = PaddingValues(),
    orientation: Orientation = Orientation.Horizontal,
    fillCenter: Boolean = true,
    icon: @Composable RowScope.() -> Unit = {},
    title: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) = when (orientation) {
    Orientation.Horizontal -> Row(
        modifier = modifier.wrapContentHeight().padding(contentPadding),
        verticalAlignment = contentAlignment,
    ) {
        Row(
            modifier = Modifier.wrapContentHeight().padding(iconPadding),
            verticalAlignment = iconVerticalAlignment,
            horizontalArrangement = iconHorizontalArrangement,
            content = icon
        )
        Row(
            modifier = Modifier
                .conditional(fillCenter) {
                    weight(1f)
                }
                .wrapContentHeight()
                .padding(titlePadding),
            verticalAlignment = titleVerticalAlignment,
            horizontalArrangement = titleHorizontalArrangement,
            content = title
        )
        Row(
            modifier = Modifier.wrapContentHeight().padding(actionsPadding),
            verticalAlignment = actionsVerticalAlignment,
            horizontalArrangement = actionsHorizontalArrangement,
            content = actions
        )
    }

    Orientation.Vertical -> Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .padding(contentPadding),
    ) {
        Row(
            modifier = Modifier.width(IntrinsicSize.Min).padding(iconPadding),
            verticalAlignment = iconVerticalAlignment,
            content = icon
        )
        Row(
            modifier = Modifier
                .conditional(fillCenter) {
                    weight(1f)
                }
                .wrapContentHeight()
                .padding(titlePadding),
            verticalAlignment = titleVerticalAlignment,
            content = title
        )
        Row(
            modifier = Modifier.width(IntrinsicSize.Min)
                .padding(actionsPadding),
            verticalAlignment = actionsVerticalAlignment,
            content = actions
        )
    }
}

@Preview
@Composable
fun AppBarHorizontalPreview() = preview {
    AppBar(
        modifier = Modifier.height(48.dp).background(Color.Green),
        orientation = Orientation.Horizontal,
        fillCenter = false,
        icon = {
            Box(
                modifier = Modifier.size(24.dp).background(Color.Red)
            )
        },
        title = {
            Box(
                modifier = Modifier.size(24.dp).background(Color.Blue)
            )
        },
        actions = {
            Box(
                modifier = Modifier.size(24.dp, 24.dp).background(Color.Yellow)
            )
        }
    )
}

@Preview
@Composable
fun AppBarHorizontalFillPreview() = preview {
    AppBar(
        modifier = Modifier.height(48.dp).background(Color.Green),
        orientation = Orientation.Horizontal,
        fillCenter = true,
        icon = {
            Box(
                modifier = Modifier.size(24.dp).background(Color.Red)
            )
        },
        title = {
            Box(
                modifier = Modifier.height(24.dp).fillMaxWidth().background(Color.Blue)
            )
        },
        actions = {
            Box(
                modifier = Modifier.size(24.dp, 24.dp).background(Color.Yellow)
            )
        }
    )
}

@Preview
@Composable
fun AppBarVerticalPreview() = preview {
    AppBar(
        modifier = Modifier.background(Color.Green),
        orientation = Orientation.Vertical,
        fillCenter = false,
        icon = {
            Box(
                modifier = Modifier.size(24.dp).background(Color.Red)
            )
        },
        title = {
            Box(
                modifier = Modifier.size(24.dp).background(Color.Blue)
            )
        },
        actions = {
            Box(
                modifier = Modifier.size(24.dp).background(Color.Yellow)
            )
        }
    )
}

@Preview
@Composable
fun AppBarVerticalFillPreview() = preview {
    AppBar(
        modifier = Modifier.background(Color.Green),
        orientation = Orientation.Vertical,
        fillCenter = true,
        icon = {
            Box(
                modifier = Modifier.size(24.dp).background(Color.Red)
            )
        },
        title = {
            Box(
                modifier = Modifier.width(24.dp).fillMaxHeight().background(Color.Blue)
            )
        },
        actions = {
            Box(
                modifier = Modifier.size(24.dp).background(Color.Yellow)
            )
        }
    )
}
