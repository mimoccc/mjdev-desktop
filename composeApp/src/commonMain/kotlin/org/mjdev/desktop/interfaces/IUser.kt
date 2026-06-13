package org.mjdev.desktop.interfaces

import okio.Path
import org.mjdev.desktop.data.DesktopConfig
import org.mjdev.desktop.icons.user.AccountCircle

interface IUser {
    val userName: String
    val password: String
    val uid: String
    val gid: String
    val description: String
    val home: String
    val shell: String
    val picture: Any
    val homeDir: Path
    val isLoggedIn: Boolean
    val userDirs: IUserDirs
    val config: DesktopConfig
        get() = DesktopConfig.load(this) // todo load
    val backgrounds
        get() = config.desktopBackgrounds
    val theme: ITheme

    companion object {
        private val NOBODY =
            object : IUser {
                override val userName: String = "nobody"
                override val password: String = ""
                override val uid: String = "nobody"
                override val gid: String = "nobody"
                override val description: String = "nobody"
                override val home: String = "nobody"
                override val shell: String = ""
                override val picture: Any = AccountCircle
                override val isLoggedIn: Boolean = true
                override val userDirs: IUserDirs = IUserDirs.DEFAULT
                override val theme: ITheme = ITheme.DEFAULT
                override val homeDir: Path = userDirs.homeDirectory
            }
        val DEFAULT = NOBODY
    }
}
