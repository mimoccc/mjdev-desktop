package org.psippr.desktop.helpers.files

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Regression: in a psippr session a folder opened in a random app claiming folders, because the
 * user's default (thunar) was not installed and psippr has no list of its own.
 */
class MimeAppsLookupTest {
    private val root: File = Files.createTempDirectory("mimeapps").toFile()
    private val home = File(root, "home")
    private val system = File(root, "usr/share")

    private val env =
        mapOf(
            "XDG_CURRENT_DESKTOP" to "psippr",
            "XDG_CONFIG_DIRS" to File(root, "etc/xdg").path,
            "XDG_DATA_DIRS" to system.path,
        )

    private fun write(
        file: File,
        text: String,
    ) {
        file.parentFile.mkdirs()
        file.writeText(text)
    }

    private fun lookup() = MimeAppsLookup(env = { name -> env[name] }, home = home.path)

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    @Test
    fun missingUserDefaultFallsBackToTheDistroList() {
        write(File(home, ".config/mimeapps.list"), "[Default Applications]\ninode/directory=thunar.desktop\n")
        write(
            File(system, "applications/gnome-mimeapps.list"),
            "[Default Applications]\ninode/directory=org.gnome.Nautilus.desktop\n",
        )
        write(File(system, "applications/org.gnome.Nautilus.desktop"), "[Desktop Entry]\n")
        write(File(system, "applications/io.example.Claimer.desktop"), "[Desktop Entry]\nMimeType=inode/directory;\n")
        assertEquals("org.gnome.Nautilus.desktop", lookup().defaultApp("inode/directory")?.name)
    }

    @Test
    fun installedUserDefaultWins() {
        write(File(home, ".config/mimeapps.list"), "[Default Applications]\ninode/directory=thunar.desktop\n")
        write(File(home, ".local/share/applications/thunar.desktop"), "[Desktop Entry]\n")
        write(
            File(system, "applications/gnome-mimeapps.list"),
            "[Default Applications]\ninode/directory=org.gnome.Nautilus.desktop\n",
        )
        write(File(system, "applications/org.gnome.Nautilus.desktop"), "[Desktop Entry]\n")
        assertEquals("thunar.desktop", lookup().defaultApp("inode/directory")?.name)
    }

    @Test
    fun noInstalledDefaultMeansNoApp() {
        write(File(home, ".config/mimeapps.list"), "[Default Applications]\ninode/directory=thunar.desktop\n")
        assertNull(lookup().defaultApp("inode/directory"))
    }
}
