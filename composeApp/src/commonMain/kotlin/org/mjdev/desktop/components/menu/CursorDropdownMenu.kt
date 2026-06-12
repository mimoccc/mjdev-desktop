package org.mjdev.desktop.components.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

// todo
@Composable
fun CursorDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) = AnimatedVisibility(
    visible = expanded,
    modifier = modifier
) {
    content()
}

// todo preview
