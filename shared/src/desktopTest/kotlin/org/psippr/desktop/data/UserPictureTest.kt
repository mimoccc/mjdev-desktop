package org.psippr.desktop.data

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Regression tests of the missing user picture: GNOME / Ubuntu keep it in AccountsService, not ~/.face. */
class UserPictureTest {
    private val root: File = Files.createTempDirectory(TEMP_PREFIX).toFile()
    private val home = File(root, HOME_DIR).apply { mkdirs() }
    private val icons = File(root, ICONS_DIR).apply { mkdirs() }

    private fun File.touch(): File = apply { writeText(CONTENT) }

    @Test
    fun usesAccountsServicePictureWhenThereIsNoFaceFile() {
        val accountsPicture = File(icons, USER).touch()
        assertEquals(accountsPicture, UserPicture.find(home, USER, icons))
    }

    @Test
    fun faceFileWinsOverOtherPlaces() {
        File(icons, USER).touch()
        File(home, UserPicture.FACE_ICON_FILE).touch()
        val face = File(home, UserPicture.FACE_FILE).touch()
        assertEquals(face, UserPicture.find(home, USER, icons))
    }

    @Test
    fun faceIconIsUsedWhenFaceIsMissing() {
        File(icons, USER).touch()
        val faceIcon = File(home, UserPicture.FACE_ICON_FILE).touch()
        assertEquals(faceIcon, UserPicture.find(home, USER, icons))
    }

    @Test
    fun noPictureAnywhereGivesNull() {
        assertNull(UserPicture.find(home, USER, icons))
        assertNull(UserPicture.find(home, "", icons))
    }

    private companion object {
        /** Prefix of the temporary test directory. */
        const val TEMP_PREFIX = "psippr-user-picture"

        /** Sample layout and user. */
        const val HOME_DIR = "home"
        const val ICONS_DIR = "icons"
        const val USER = "tester"

        /** Content of the fake picture files. */
        const val CONTENT = "png"
    }
}
