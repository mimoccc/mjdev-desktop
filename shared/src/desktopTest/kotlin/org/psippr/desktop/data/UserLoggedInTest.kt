package org.psippr.desktop.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Regression tests: the desktop user counts as logged in without a whoami run that can fail. */
class UserLoggedInTest {
    @Test
    fun processUserIsLoggedIn() {
        assertTrue(User.isLoggedIn(System.getProperty(USER_NAME_PROPERTY)))
    }

    @Test
    fun otherUserIsNotLoggedIn() {
        assertFalse(User.isLoggedIn(System.getProperty(USER_NAME_PROPERTY) + OTHER_SUFFIX))
    }

    @Test
    fun nobodyIsNeverLoggedIn() {
        assertFalse(User.NOBODY.isLoggedIn)
    }

    private companion object {
        /** JVM system property holding the account name of the running process. */
        const val USER_NAME_PROPERTY = "user.name"

        /** Turns the process user name into a name of another account. */
        const val OTHER_SUFFIX = "-other"
    }
}
