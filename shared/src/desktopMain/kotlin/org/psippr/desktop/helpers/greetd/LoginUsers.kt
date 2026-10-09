package org.psippr.desktop.helpers.greetd

import org.psippr.desktop.data.User
import org.psippr.desktop.interfaces.IUser
import java.io.File

/**
 * Human accounts the login screen offers, read from the passwd database: regular uid range and
 * a real login shell, so system and service accounts never show up.
 */
object LoginUsers {
    /** System passwd database. */
    private const val PASSWD_FILE = "/etc/passwd"

    /** Field separator of a passwd line. */
    private const val SEPARATOR = ":"

    /** Index of the user name field. */
    private const val NAME_FIELD = 0

    /** Index of the uid field. */
    private const val UID_FIELD = 2

    /** Index of the login shell field. */
    private const val SHELL_FIELD = 6

    /** Number of fields in a passwd line. */
    private const val FIELD_COUNT = 7

    /** Regular (human) uid range of Debian / Ubuntu. */
    private val HUMAN_UIDS = 1000..59999

    /** Shell suffixes of accounts that cannot log in. */
    private val NO_LOGIN_SHELLS = listOf("nologin", "false")

    /** User names that can log in, in passwd order. */
    fun names(passwd: File = File(PASSWD_FILE)): List<String> = runCatching { parse(passwd.readLines()) }.getOrDefault(emptyList())

    /** Accounts that can log in (name, home, picture), in passwd order. */
    fun users(passwd: File = File(PASSWD_FILE)): List<IUser> =
        runCatching { passwd.readLines() }.getOrDefault(emptyList()).filter { line -> isHuman(line.split(SEPARATOR)) }.map(::User)

    /** User names that can log in from passwd [lines]. */
    fun parse(lines: List<String>): List<String> = lines
        .map { line -> line.split(SEPARATOR) }
        .filter(::isHuman)
        .map { fields -> fields[NAME_FIELD] }

    // regular uid and a real login shell
    private fun isHuman(fields: List<String>): Boolean =
        fields.size == FIELD_COUNT &&
            fields[UID_FIELD].toIntOrNull() in HUMAN_UIDS &&
            NO_LOGIN_SHELLS.none { shell -> fields[SHELL_FIELD].endsWith(shell) }
}
