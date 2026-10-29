package org.psippr.desktop.components.controlcenter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import org.psippr.desktop.interfaces.IPage

/**
 * Control center page area: keeps the page at [current] and its previous and next page composed
 * (their data already loading) but places only the current one, so the others are neither drawn
 * nor hit by the pointer. Replaces [PreloadedPages], whose pager composed every page at once and
 * drew them over each other (the last page, About, covered every tab).
 */
@Composable
fun NeighbourPages(
    pages: List<IPage>,
    current: Int,
    modifier: Modifier = Modifier,
) {
    if (pages.isEmpty()) return
    val shown = current.coerceIn(pages.indices)
    val composed = NeighbourPageIndices.of(shown, pages.size)
    Layout(
        modifier = modifier,
        content = {
            composed.forEach { index ->
                key(pages[index].name) {
                    Box(modifier = Modifier.fillMaxSize()) { pages[index].Render() }
                }
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable -> measurable.measure(constraints) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.getOrNull(composed.indexOf(shown))?.place(0, 0)
        }
    }
}

