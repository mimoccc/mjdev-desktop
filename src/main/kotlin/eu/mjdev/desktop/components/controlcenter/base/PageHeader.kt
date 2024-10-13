/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.controlcenter.base

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.mjdev.desktop.components.appbar.AppBar
import eu.mjdev.desktop.components.controlcenter.pages.MainSettingsPage
import eu.mjdev.desktop.components.image.ImageAny
import eu.mjdev.desktop.components.text.TextAny
import eu.mjdev.desktop.extensions.Modifier.circleBorder
import eu.mjdev.desktop.extensions.Modifier.clipCircle
import eu.mjdev.desktop.provider.DesktopScope.Companion.withDesktopScope

@Composable
fun PageHeader(
    modifier: Modifier = Modifier,
    page: ControlCenterPage = MainSettingsPage(),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    iconSize: Dp = 64.dp,
    actions: @Composable RowScope.() -> Unit = {},
) = withDesktopScope {
    if (page.showHeader) {
        AppBar(
            modifier = modifier.fillMaxWidth()
                .wrapContentHeight()
                .background(backgroundColor),
            contentPadding = contentPadding,
            icon = {
                Box(
                    modifier = Modifier.size(iconSize + 4.dp)
                        .circleBorder(2.dp, textColor)
                        .padding(4.dp)
                ) {
                    ImageAny(
                        modifier = Modifier.size(iconSize).background(backgroundColor).clipCircle(),
                        src = page.icon,
                        colorFilter = ColorFilter.tint(textColor),
                        contentDescription = ""
                    )
                }
            },
            title = {
                TextAny(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                    text = page.name,
                    color = textColor,
                    fontSize = 20.sp
                )
            },
            actions = actions
        )
    }
}

@Preview
@Composable
fun PageHeaderPreview() = PageHeader()
