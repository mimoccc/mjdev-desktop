package org.psippr.desktop.helpers.greetd

import kotlin.test.Test
import kotlin.test.assertEquals

/** Regression test: the login screen lists human accounts only, never service accounts. */
class LoginUsersTest {
    @Test
    fun onlyHumanAccountsWithLoginShell() {
        val lines =
            listOf(
                "root:x:0:0:root:/root:/bin/bash",
                "_greetd:x:110:110::/var/lib/greetd:/usr/sbin/nologin",
                "psippr:x:1000:1000:psippr:/home/psippr:/bin/bash",
                "backup:x:1001:1001::/home/backup:/bin/false",
                "nobody:x:65534:65534:nobody:/nonexistent:/usr/sbin/nologin",
            )
        assertEquals(listOf("psippr"), LoginUsers.parse(lines))
    }
}
