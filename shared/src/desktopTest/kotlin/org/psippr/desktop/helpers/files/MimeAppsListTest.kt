package org.psippr.desktop.helpers.files

import kotlin.test.Test
import kotlin.test.assertEquals

/** Only the default group of a mimeapps.list names default apps. */
class MimeAppsListTest {
    private val list =
        """
        [Default Applications]
        # comment
        inode/directory=thunar.desktop;org.gnome.Nautilus.desktop;
        [Added Associations]
        text/plain=org.gnome.TextEditor.desktop;
        """.trimIndent()

    @Test
    fun defaultsAreReadInOrder() {
        assertEquals(
            listOf("thunar.desktop", "org.gnome.Nautilus.desktop"),
            MimeAppsList.defaults(list, "inode/directory"),
        )
    }

    @Test
    fun otherGroupsAreNotDefaults() {
        assertEquals(emptyList(), MimeAppsList.defaults(list, "text/plain"))
    }
}
