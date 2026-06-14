package org.mjdev.desktop.data

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Environment
import okio.Path
import okio.Path.Companion.toPath
import org.mjdev.desktop.context.DesktopContext
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.icons.user.AccountCircle
import org.mjdev.desktop.interfaces.ITheme
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.interfaces.IUserDirs
import java.io.File

class User(
    val account: Account? = null,
) : IUser {
    override val userName: String
        get() = account?.name ?: ""
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
        get() =
            runCatching {
                Environment.getExternalStorageDirectory().path.toPath()
            }.getOrNull() ?: "".toPath()
    override val isLoggedIn
        get() = isLoggedIn(userName) // todo
    override val userDirs: IUserDirs
        get() = UserDirs(homeDir)
    override val config: DesktopConfig
        get() = DesktopConfig.load(this) // todo load
    override val theme: ITheme
        get() = Theme.load(this) // todo load
    override val backgrounds
        get() = config.desktopBackgrounds

    override fun toString(): String = userName

//    fun login(
//        context: IDesktopContext,
//        password: String
//    ): Flow<Boolean> = flow { context.login(userName, password) }

    companion object {
        val NOBODY = User()
        val DEFAULT = NOBODY

        fun isLoggedIn(uname: String): Boolean = false // todo

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
            if (context is DesktopContext) {
                AccountManager.get(context.context).accounts.map { a ->
                    User(a)
                }
            } else {
                emptyList()
            }
    }
}
