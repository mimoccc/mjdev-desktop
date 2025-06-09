/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.os.linux

import org.mjdev.desktop.helpers.system.shell.Shell
import org.mjdev.desktop.managers.os.base.OSManagerStub
import org.mjdev.desktop.context.IDesktopContext

class OSManagerLinux(
    context: IDesktopContext
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
    override val machineName
        get() = Shell.executeAndRead("hostname").trim()
}