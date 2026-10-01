/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.os.base

import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.managers.os.IOSManager

open class OSManagerStub(
    val context: IDesktopContext,
) : IOSManager {
    override val prettyName: String = ""
    open val name: String = ""
    open val versionId: String = ""
    open val version: String = ""
    open val versionCodeName: String = ""
    open val id: String = ""
    open val idLike: String = ""
    override val homeUrl: String = ""
    override val supportUrl: String = ""
    open val bugReportUrl: String = ""
    open val privacyPolicyUrl: String = ""
    open val codename: String = ""
    open val logo: String = ""
    override val machineName: String = ""
}
