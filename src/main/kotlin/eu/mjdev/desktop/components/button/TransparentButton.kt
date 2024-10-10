/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.components.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.mjdev.desktop.extensions.Compose.noElevation
import eu.mjdev.desktop.extensions.Compose.transparent

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