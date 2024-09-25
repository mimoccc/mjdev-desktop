package eu.mjdev.desktop.data

import eu.mjdev.desktop.helpers.system.Command
import eu.mjdev.desktop.helpers.system.UserDirs
import java.io.File

// todo
@Suppress("MemberVisibilityCanBePrivate", "unused")
class User(
    val data: String,
    val dataItems: List<String> = data.split(":"),
    val config: DesktopConfig = DesktopConfig.Default, // todo load
    val theme: Theme = Theme.Default // todo load
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
    val picture: Any?
        get() = loadPicture(dataItems[5], ".face")

    val homeDir
        get() = File(home)

    val isLoggedIn
        get() = isLoggedIn(userName)

    val userDirs
        get() = UserDirs(homeDir)

    override fun toString(): String {
        return userName
    }

    fun login(
        string: String
    ): Boolean {
        // todo
        return true
    }

    companion object {
        val Nobody = User(":x:-1:-1:::")

        fun isLoggedIn(un: String): Boolean =
            Command("whoami").execute()?.trim()?.contentEquals(un) == true

        fun loadPicture(home: String, pic: String): File = loadPicture(File(home), pic)

        fun loadPicture(homeDir: File, picName: String): File = File(homeDir, picName)

        val allUsers: List<User>
            get() = File("/etc/passwd")
                .readLines()
                .filter { t ->
                    t.contains("/home")
                }.map {
                    User(it)
                }
    }
}