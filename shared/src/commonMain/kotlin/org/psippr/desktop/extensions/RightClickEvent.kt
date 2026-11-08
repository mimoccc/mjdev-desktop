package org.psippr.desktop.extensions

import androidx.compose.ui.input.pointer.PointerEventType

/**
 * Pointer event a right click is read from: the press. Only a press still holds the right button
 * in its buttons; on the release it is already up, so a right click read there looks like a left one.
 */
val RightClickEvent: PointerEventType = PointerEventType.Press
