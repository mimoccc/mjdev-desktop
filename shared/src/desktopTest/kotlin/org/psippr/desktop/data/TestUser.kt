package org.psippr.desktop.data

import okio.Path
import okio.Path.Companion.toPath
import org.psippr.desktop.interfaces.ITheme
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.interfaces.IUserDirs

/** Test user whose home folder does not exist, so loading its config never touches real files. */
class TestUser(
    override val userName: String,
) : IUser {
    override val password: String = ""
    override val uid: String = userName
    override val gid: String = userName
    override val description: String = userName
    override val home: String = MISSING_HOME + userName
    override val shell: String = ""
    override val picture: Any = userName
    override val homeDir: Path = home.toPath()
    override val isLoggedIn: Boolean = true
    override val userDirs: IUserDirs = IUserDirs.DEFAULT
    override val theme: ITheme = ITheme.DEFAULT

    private companion object {
        /** Prefix of a home folder that never exists. */
        const val MISSING_HOME = "/nonexistent/psippr-test-home/"
    }
}
