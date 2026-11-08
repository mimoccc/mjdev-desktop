package org.psippr.desktop.helpers.files

import java.io.File

/**
 * Finds the default app of a file type the freedesktop way (user lists first, then the system's,
 * the current desktop's list before the generic one), skipping defaults whose app is not
 * installed. When none is found, the distro's desktop lists (`ubuntu-mimeapps.list`,
 * `gnome-mimeapps.list`, ...) are asked too, because a psippr session has no list of its own.
 *
 * @property env environment variables
 * @property home the home folder
 */
class MimeAppsLookup(
    private val env: (String) -> String? = System::getenv,
    private val home: String = System.getProperty(PROPERTY_HOME).orEmpty(),
) {
    /** Desktop file of the default app for [mimeType], or null when no installed app is set. */
    fun defaultApp(mimeType: String): File? = listFiles()
        .asSequence()
        .filter { file -> file.isFile }
        .flatMap { file -> MimeAppsList.defaults(runCatching { file.readText() }.getOrDefault(""), mimeType) }
        .mapNotNull { id -> desktopFile(id) }
        .firstOrNull()

    /** The lists asked, in order. */
    fun listFiles(): List<File> = (configDirs() + appDirs())
        .flatMap { dir -> desktops().map { name -> File(dir, "$name$DESKTOP_LIST_SUFFIX") } + File(dir, LIST_NAME) } +
        distroLists()

    /** Installed desktop file of the entry [id], or null. */
    fun desktopFile(id: String): File? = appDirs()
        .map { dir -> File(dir, id) }
        .firstOrNull { file -> file.isFile }

    /** The current desktops, lower case (`XDG_CURRENT_DESKTOP=psippr` -> `psippr`). */
    private fun desktops(): List<String> = env(VAR_CURRENT_DESKTOP)
        .orEmpty()
        .split(DESKTOP_SEPARATOR)
        .map { name -> name.trim().lowercase() }
        .filter { name -> name.isNotEmpty() }

    /** Config folders holding lists. */
    private fun configDirs(): List<File> = (XdgDir.CONFIG_HOME.paths(env, home) + XdgDir.CONFIG_DIRS.paths(env, home))
        .map { path -> File(path) }

    /** Folders holding desktop files (and lists). */
    private fun appDirs(): List<File> = (XdgDir.DATA_HOME.paths(env, home) + XdgDir.DATA_DIRS.paths(env, home))
        .map { path -> File(path, APPLICATIONS) }

    /** Every desktop's list in the system app folders, the distro's defaults. */
    private fun distroLists(): List<File> = XdgDir.DATA_DIRS
        .paths(env, home)
        .map { path -> File(path, APPLICATIONS) }
        .flatMap { dir ->
            dir
                .listFiles { file -> file.name.endsWith(DESKTOP_LIST_SUFFIX) }
                .orEmpty()
                .sortedBy { file -> file.name }
        }

    private companion object {
        /** System property of the home folder. */
        const val PROPERTY_HOME = "user.home"

        /** Variable naming the current desktops. */
        const val VAR_CURRENT_DESKTOP = "XDG_CURRENT_DESKTOP"

        /** Separates desktops in [VAR_CURRENT_DESKTOP]. */
        const val DESKTOP_SEPARATOR = ':'

        /** Folder of desktop files under a data folder. */
        const val APPLICATIONS = "applications"

        /** The generic list. */
        const val LIST_NAME = "mimeapps.list"

        /** Ending of a desktop's own list (`gnome-mimeapps.list`). */
        const val DESKTOP_LIST_SUFFIX = "-mimeapps.list"
    }
}
