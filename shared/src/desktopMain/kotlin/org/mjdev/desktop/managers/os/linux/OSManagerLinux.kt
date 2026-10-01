/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.os.linux

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.managers.os.base.OSManagerStub
import java.io.File

class OSManagerLinux(
    context: IDesktopContext,
) : OSManagerStub(context) {
    private val osRelease = OsRelease()

    override val prettyName
        get() = osRelease.prettyName
    override val name
        get() = osRelease.name
    override val versionId
        get() = osRelease.versionId
    override val version
        get() = osRelease.version
    override val versionCodeName
        get() = osRelease.versionCodeName
    override val id
        get() = osRelease.id
    override val idLike
        get() = osRelease.idLike
    override val homeUrl
        get() = osRelease.homeUrl
    override val supportUrl
        get() = osRelease.supportUrl
    override val bugReportUrl
        get() = osRelease.bugReportUrl
    override val privacyPolicyUrl
        get() = osRelease.privacyPolicyUrl
    override val codename
        get() = osRelease.ubuntuCodename
    override val logo
        get() = osRelease.logo
    override val kernel: String
        get() = Shell.executeAndRead("uname", "-r").trim()
    override val uptime: String
        get() = runCatching {
            File(UPTIME_FILE)
                .readText()
                .substringBefore(' ')
                .toDouble()
                .toLong()
                .let { total ->
                    val days = total / SECONDS_PER_DAY
                    val hours = total % SECONDS_PER_DAY / SECONDS_PER_HOUR
                    val minutes = total % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
                    "${days}d ${hours}h ${minutes}m"
                }
        }.getOrDefault("")
    override val machineName
        get() = Shell.executeAndRead("hostname").trim()

    companion object {
        /** Kernel file holding the uptime in seconds as its first field. */
        private const val UPTIME_FILE = "/proc/uptime"
        private const val SECONDS_PER_DAY = 86_400L
        private const val SECONDS_PER_HOUR = 3_600L
        private const val SECONDS_PER_MINUTE = 60L
    }
}
