package org.psippr.desktop.helpers.greetd

import java.io.File
import java.util.Properties

/**
 * Remembers the last logged in user and the session each user picked, in the greeter account's
 * home (`/var/lib/greetd`, owned by the greeter), so the login screen preselects them.
 */
class LastLoginStore(
    private val file: File = File(System.getProperty(HOME_PROPERTY), FILE_PATH),
) {
    private val properties = Properties().apply { runCatching { file.inputStream().use(::load) } }

    /** User of the last successful login, null before the first one. */
    val lastUser: String?
        get() = properties.getProperty(KEY_LAST_USER)

    /** Session name [user] logged into last time, null before the first login. */
    fun lastSession(user: String): String? = properties.getProperty(KEY_SESSION_PREFIX + user)

    /** Stores a successful login of [user] into the session named [session]. */
    fun remember(
        user: String,
        session: String,
    ) = runCatching {
        properties.setProperty(KEY_LAST_USER, user)
        properties.setProperty(KEY_SESSION_PREFIX + user, session)
        file.parentFile?.mkdirs()
        file.outputStream().use { out -> properties.store(out, null) }
    }

    private companion object {
        /** JVM property holding the greeter account's home. */
        const val HOME_PROPERTY = "user.home"

        /** Store file inside the home. */
        const val FILE_PATH = ".cache/psippr-desktop/last-login.properties"

        /** Keys of the store. */
        const val KEY_LAST_USER = "user"
        const val KEY_SESSION_PREFIX = "session."
    }
}
