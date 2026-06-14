package org.mjdev.desktop.components

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext

@Preview
@Composable
fun OrientatedView(
    modifier: Modifier = Modifier,
    landscape: @Composable BoxScope.() -> Unit = {},
    portrait: @Composable BoxScope.() -> Unit = {},
) = withDesktopContext {
    Box(
        modifier = modifier
    ) {
        if (context.platformContext?.resources?.configuration?.orientation == ORIENTATION_PORTRAIT) {
            portrait()
        } else {
            landscape()
        }
    }
}