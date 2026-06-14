/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.controlcenter.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.appbar.AppBar
import org.mjdev.desktop.components.image.ImageAny
import org.mjdev.desktop.components.text.TextAny
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.Modifier.circleBorder
import org.mjdev.desktop.extensions.Modifier.clipCircle

@Composable
fun PageHeader(
    modifier: Modifier = Modifier,
    page: ControlCenterPage = ControlCenterPage(LocalDesktopContext.current),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    iconSize: Dp = 64.dp,
    actions: @Composable RowScope.() -> Unit = {},
) = withDesktopContext {
    if (page.showHeader) {
        AppBar(
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(backgroundColor),
            contentPadding = contentPadding,
            icon = {
                Box(
                    modifier =
                        Modifier
                            .size(iconSize + 4.dp)
                            .circleBorder(2.dp, textColor)
                            .padding(4.dp),
                ) {
                    ImageAny(
                        modifier = Modifier.size(iconSize).background(backgroundColor).clipCircle(),
                        src = page.icon,
                        colorFilter = ColorFilter.tint(textColor),
                        contentDescription = "",
                    )
                }
            },
            title = {
                TextAny(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                    text = page.name,
                    color = textColor,
                    fontSize = 20.sp,
                )
            },
            actions = actions,
        )
    }
}

@Preview
@Composable
fun PreviewPageHeader() =
    preview {
        PageHeader(
            page = ControlCenterPage(context),
        )
    }
