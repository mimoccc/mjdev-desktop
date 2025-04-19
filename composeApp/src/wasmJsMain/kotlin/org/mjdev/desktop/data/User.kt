package org.mjdev.desktop.data

import okio.Path
import okio.Path.Companion.toPath
import org.mjdev.desktop.icons.user.AccountCircle
import org.mjdev.desktop.interfaces.IDesktopContext
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.interfaces.IUserDirs

class User() : IUser {
    override val userName: String
        get() = "-" // todo
    override val password: String
        get() = "" // todo
    override val uid: String
        get() = "" // todo
    override val gid: String
        get() = "" // todo
    override val description: String
        get() = "" // todo
    override val home: String
        get() = "/sdcard/" // todo
    override val shell: String
        get() = "" // todo
    override val picture: Any
        get() = AccountCircle // todo
    override val homeDir: Path
        get() = "/".toPath() // todo
    override val isLoggedIn
        get() = isLoggedIn(userName) // todo
    override val userDirs: IUserDirs
        get() = IUserDirs.DEFAULT
    override val config: DesktopConfig
        get() = DesktopConfig.load(this) // todo load
    override val theme: ITheme
        get() = ITheme.DEFAULT // Theme.load(this) // todo load
    override val backgrounds
        get() = config.desktopBackgrounds

    override fun toString(): String {
        return userName
    }

//    fun login(
//        context: IDesktopContext,
//        password: String
//    ): Flow<Boolean> = flow { context.login(userName, password) }

    companion object {
        val NOBODY = User()
        val DEFAULT = NOBODY

        fun isLoggedIn(
            uname: String
        ): Boolean = false // todo

        fun allUsers(
            context: IDesktopContext
        ): List<User> = emptyList()
    }
}
