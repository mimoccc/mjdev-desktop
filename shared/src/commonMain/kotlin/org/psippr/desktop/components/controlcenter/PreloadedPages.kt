package org.psippr.desktop.components.controlcenter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.psippr.desktop.interfaces.IPage

/** Pages kept composed on each side of the shown one, so switching to a neighbour is instant. */
private const val PRELOADED_NEIGHBOURS = 1

/**
 * Control center page area: shows [pages] at [current] and keeps the previous and next page
 * composed (their data already loaded). Switching is driven by the tab icons only, instantly.
 */
@Composable
fun PreloadedPages(
    pages: List<IPage>,
    current: Int,
    modifier: Modifier = Modifier,
) {
    val state = rememberPagerState(initialPage = current) { pages.size }
    LaunchedEffect(current, pages.size) {
        if (current in pages.indices && state.currentPage != current) state.scrollToPage(current)
    }
    HorizontalPager(
        state = state,
        modifier = modifier,
        beyondViewportPageCount = PRELOADED_NEIGHBOURS,
        userScrollEnabled = false,
        key = { index -> pages[index].name },
    ) { index ->
        pages[index].Render()
    }
}
