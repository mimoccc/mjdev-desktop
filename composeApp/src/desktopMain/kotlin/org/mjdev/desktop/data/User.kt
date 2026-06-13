package org.mjdev.desktop.data

import okio.Path
import okio.Path.Companion.toPath
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.PathExt.lines
import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.icons.user.AccountCircle
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.interfaces.IUserDirs
import java.io.File

@Suppress("MemberVisibilityCanBePrivate", "unused")
class User(
    val data: String,
    val dataItems: List<String> = data.split(":"),
) : IUser {
    override val userName: String
        get() = dataItems[0]
    override val password: String
        get() = dataItems[1]
    override val uid: String
        get() = dataItems[2]
    override val gid: String
        get() = dataItems[3]
    override val description: String
        get() = dataItems[4]
    override val home: String
        get() = dataItems[5]
    override val shell: String
        get() = dataItems[6]
    override val picture: Any
        get() = loadPicture(dataItems[5], ".face") ?: AccountCircle
    override val homeDir: Path
        get() = home.toPath()
    override val isLoggedIn
        get() = isLoggedIn(userName)
    override val userDirs: IUserDirs
        get() = UserDirs(homeDir)
    override val config: DesktopConfig
        get() = DesktopConfig.load(this) // todo load
    override val theme: Theme
        get() = Theme.load(this) // todo load
    override val backgrounds
        get() = config.desktopBackgrounds

    override fun toString(): String = userName

//    fun login(
//        context: IDesktopContext,
//        password: String
//    ): Flow<Boolean> = flow { context.login(userName, password) }

    companion object {
        val NOBODY = User(":x:-1:-1:::")
        val DEFAULT = NOBODY

        fun isLoggedIn(uname: String): Boolean = Shell.executeAndRead("whoami").trim().contentEquals(uname)

        fun loadPicture(
            home: String,
            pic: String,
        ): File? = loadPicture(File(home), pic)

        fun loadPicture(
            homeDir: File,
            picName: String,
        ): File? =
            File(homeDir, picName).let { f ->
                if (f.exists()) f else null
            }

        fun allUsers(context: IDesktopContext): List<User> =
            "/etc/passwd"
                .toPath()
                .lines
                .filter { t ->
                    t.contains("/home")
                }.map { dir ->
                    User(dir)
                }
    }
}
