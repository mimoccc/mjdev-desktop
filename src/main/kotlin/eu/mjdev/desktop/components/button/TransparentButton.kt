/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.button

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.SuperDarkGray
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Compose.transparent
import eu.mjdev.desktop.icons.Icons

@Composable
fun TransparentButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit = {}
) = Button(
    modifier = modifier,
    contentPadding = PaddingValues(0.dp),
    onClick = onClick,
    colors = ButtonDefaults.transparent(),
    elevation = ButtonDefaults.noElevation(),
    content = content
)

@Preview
@Composable
fun TransparentButtonPreview() = Box(
    modifier = Modifier.fillMaxSize().background(Color.SuperDarkGray)
) {
    TransparentButton {
        Icon(
            imageVector = Icons.User,
            contentDescription = "",
            tint = Color.White
        )
    }
}