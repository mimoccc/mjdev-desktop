package org.psippr.desktop.managers.theme.linux.icons

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The index lists every folder, inherits the user's theme and marks its generator version. */
class IconThemeIndexTest {
    private val index = IconThemeIndex.build("Psippr-Font", "Font", inherits = "Yaru")

    @Test
    fun inheritsTheUsersThemeThenHicolor() {
        assertTrue(index.lines().contains("Inherits=Yaru,hicolor"))
    }

    @Test
    fun everyFolderHasASection() {
        IconSize.entries.forEach { size ->
            IconContext.entries.forEach { context ->
                assertTrue(index.contains("[${IconThemeIndex.dirOf(size, context)}]"))
            }
        }
    }

    @Test
    fun onlyThisVersionIsCurrent() {
        assertTrue(IconThemeIndex.isCurrent(index))
        assertFalse(IconThemeIndex.isCurrent(index.replace("${IconThemeIndex.GENERATOR_KEY}=", "X-Other=")))
    }
}
