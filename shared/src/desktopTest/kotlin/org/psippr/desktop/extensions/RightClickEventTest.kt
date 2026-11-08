package org.psippr.desktop.extensions

import androidx.compose.ui.input.pointer.PointerEventType
import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression: the desktop read right clicks on the release, so its menu never opened. */
class RightClickEventTest {
    @Test
    fun rightClickIsReadOnThePress() {
        assertEquals(PointerEventType.Press, RightClickEvent)
    }
}
