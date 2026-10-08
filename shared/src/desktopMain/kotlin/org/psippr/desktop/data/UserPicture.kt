package org.psippr.desktop.data

import java.io.File

/**
 * Finds the account picture of a Linux user. Desktops store it in different places: KDE and
 * others use `~/.face` (or `~/.face.icon`), GNOME / Ubuntu Settings keep it in AccountsService
 * (`/var/lib/AccountsService/icons/<user>`, readable by everyone). The first existing one wins.
 */
object UserPicture {
    /** Classic picture file in the home directory. */
    const val FACE_FILE = ".face"

    /** Alternative picture file in the home directory (older display managers, KDE). */
    const val FACE_ICON_FILE = ".face.icon"

    /** AccountsService directory holding one picture per user name (GNOME, Ubuntu). */
    const val ACCOUNTS_ICONS_DIR = "/var/lib/AccountsService/icons"

    /**
     * Picture file of the user [userName] with home [homeDir], or null when there is none.
     *
     * @param accountsIconsDir AccountsService icons directory (replaceable for tests)
     */
    fun find(
        homeDir: File,
        userName: String,
        accountsIconsDir: File = File(ACCOUNTS_ICONS_DIR),
    ): File? = candidates(homeDir, userName, accountsIconsDir).firstOrNull { file -> file.isFile && file.canRead() }

    // places to look, in order of preference
    private fun candidates(
        homeDir: File,
        userName: String,
        accountsIconsDir: File,
    ): List<File> = listOfNotNull(
        File(homeDir, FACE_FILE),
        File(homeDir, FACE_ICON_FILE),
        userName.takeIf { it.isNotBlank() }?.let { name -> File(accountsIconsDir, name) },
    )
}
