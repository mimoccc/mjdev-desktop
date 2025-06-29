/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import org.mjdev.desktop.components.text.TextAny

@Composable
fun WebView(
    url: String = "https://google.com",
    modifier: Modifier = Modifier,
) = withDesktopContext {
    Box(
        modifier = modifier.background(backgroundColor)
    ) {
        TextAny(
            text = "WebView is not supported on this platform.\n$url",
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Left,
            color = textColor,
        )
    }
}