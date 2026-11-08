package org.psippr.desktop.helpers.files

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** A file is only handed to an app when the system names one for its type. */
class FileAssociationTest {
    @Test
    fun defaultAppIsTakenFromTheAnswer() {
        assertEquals("org.gnome.TextEditor.desktop", FileAssociation.appIdOf("org.gnome.TextEditor.desktop\n"))
    }

    @Test
    fun emptyAnswerMeansNoApp() {
        assertNull(FileAssociation.appIdOf(""))
        assertNull(FileAssociation.appIdOf("\n"))
    }
}
