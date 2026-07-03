package org.mjdev.desktop.managers.remote

import java.awt.event.KeyEvent

/**
 * Translates X11 keysyms (what an RFB/VNC client sends in a KeyEvent) into AWT virtual key
 * codes for java.awt.Robot. Printable Latin-1 keysyms equal their Unicode code point and are
 * handled generically; only the non-printable/function keys need this explicit table.
 */
object RfbKeymap {
    private val special: Map<Long, Int> =
        mapOf(
            0xFF08L to KeyEvent.VK_BACK_SPACE,
            0xFF09L to KeyEvent.VK_TAB,
            0xFF0DL to KeyEvent.VK_ENTER,
            0xFF1BL to KeyEvent.VK_ESCAPE,
            0xFF63L to KeyEvent.VK_INSERT,
            0xFFFFL to KeyEvent.VK_DELETE,
            0xFF50L to KeyEvent.VK_HOME,
            0xFF57L to KeyEvent.VK_END,
            0xFF55L to KeyEvent.VK_PAGE_UP,
            0xFF56L to KeyEvent.VK_PAGE_DOWN,
            0xFF51L to KeyEvent.VK_LEFT,
            0xFF52L to KeyEvent.VK_UP,
            0xFF53L to KeyEvent.VK_RIGHT,
            0xFF54L to KeyEvent.VK_DOWN,
            0xFFE1L to KeyEvent.VK_SHIFT,
            0xFFE2L to KeyEvent.VK_SHIFT,
            0xFFE3L to KeyEvent.VK_CONTROL,
            0xFFE4L to KeyEvent.VK_CONTROL,
            0xFFE9L to KeyEvent.VK_ALT,
            0xFFEAL to KeyEvent.VK_ALT,
            0xFFEBL to KeyEvent.VK_META,
            0xFFECL to KeyEvent.VK_META,
            0xFF20L to KeyEvent.VK_CAPS_LOCK,
            0xFFBEL to KeyEvent.VK_F1,
            0xFFBFL to KeyEvent.VK_F2,
            0xFFC0L to KeyEvent.VK_F3,
            0xFFC1L to KeyEvent.VK_F4,
            0xFFC2L to KeyEvent.VK_F5,
            0xFFC3L to KeyEvent.VK_F6,
            0xFFC4L to KeyEvent.VK_F7,
            0xFFC5L to KeyEvent.VK_F8,
            0xFFC6L to KeyEvent.VK_F9,
            0xFFC7L to KeyEvent.VK_F10,
            0xFFC8L to KeyEvent.VK_F11,
            0xFFC9L to KeyEvent.VK_F12,
        )

    /** The AWT key code for a keysym, or null if it cannot be typed via a virtual key. */
    fun keyCode(keysym: Long): Int? {
        special[keysym]?.let { return it }
        // printable Latin-1 (0x20..0xFE) == the Unicode char; let AWT resolve its VK
        if (keysym in 0x20..0xFF) {
            val code = KeyEvent.getExtendedKeyCodeForChar(keysym.toInt())
            if (code != KeyEvent.VK_UNDEFINED) return code
        }
        return null
    }
}
