/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.mjdev.desktop.extensions.ButtonDefaults.noElevation
import org.mjdev.desktop.extensions.ButtonDefaults.transparent
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.icons.user.AccountCircle

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

//@Preview
@Suppress("unused")
@Composable
fun TransparentButtonPreview() = preview {
    TransparentButton {
        Icon(
            imageVector = AccountCircle,
            contentDescription = "",
            tint = Color.White
        )
    }
}