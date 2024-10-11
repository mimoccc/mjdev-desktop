package eu.mjdev.desktop.data

import eu.mjdev.desktop.extensions.Custom.lines
import eu.mjdev.desktop.helpers.system.Shell
import eu.mjdev.desktop.helpers.system.UserDirs
import eu.mjdev.desktop.icons.Icons
import eu.mjdev.desktop.provider.DesktopProvider
import java.io.File

@Suppress("MemberVisibilityCanBePrivate", "unused")
class User(
    val data: String,
    val dataItems: List<String> = data.split(":"),
) {
    val userName: String
        get() = dataItems[0]
    val password: String
        get() = dataItems[1]
    val uid: String
        get() = dataItems[2]
    val gid: String
        get() = dataItems[3]
    val description: String
        get() = dataItems[4]
    val home: String
        get() = dataItems[5]
    val shell: String
        get() = dataItems[6]
    val picture: Any
        get() = loadPicture(dataItems[5], ".face") ?: Icons.User
    val homeDir
        get() = File(home)
    val isLoggedIn
        get() = isLoggedIn(userName)
    val userDirs
        get() = UserDirs(homeDir)
    val config: DesktopConfig
        get() = DesktopConfig.load(this) // todo load
    val theme: Theme
        get() = Theme.load(this) // todo load
    val backgrounds
        get() = config.desktopBackgrounds

    override fun toString(): String {
        return userName
    }

    fun login(
        api: DesktopProvider, password: String
    ): Boolean = api.login(userName, password)

    companion object {
        val Nobody = User(":x:-1:-1:::")

        fun isLoggedIn(
            un: String
        ): Boolean = Shell.executeAndRead("whoami").trim().contentEquals(un)

        fun loadPicture(
            home: String, pic: String
        ): File? = loadPicture(File(home), pic)

        fun loadPicture(
            homeDir: File, picName: String
        ): File? = File(homeDir, picName).let { f ->
            if (f.exists()) f else null
        }

        val allUsers: List<User>
            get() = File("/etc/passwd").lines.filter { t ->
                t.contains("/home")
            }.map {
                User(it)
            }
    }
}