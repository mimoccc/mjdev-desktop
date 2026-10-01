package org.mjdev.desktop.plugins.remote

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import org.mjdev.desktop.context.DesktopContextScope.Companion.withDesktopContext
import org.mjdev.desktop.plugins.remote.RemoteOpPainter.paint

/**
 * Renders a [RemoteDocument] with the live [variables]. Colors resolve from the wallpaper palette
 * on every frame, so the widget re-colors itself when the background changes.
 */
@Suppress("FunctionName")
@Composable
fun RemoteDocumentPlayer(
    document: RemoteDocument,
    variables: RemoteVariables,
    modifier: Modifier = Modifier,
) = withDesktopContext {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        paint(document, variables, palette, textMeasurer)
    }
}
