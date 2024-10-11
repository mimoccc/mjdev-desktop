/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.managers.os.base

import eu.mjdev.desktop.provider.DesktopProvider

open class OSManagerStub(
    val api: DesktopProvider
) {
    open val prettyName: String
        get() = ""
    open val name: String
        get() = ""
    open val versionId: String
        get() = ""
    open val version: String
        get() = ""
    open val versionCodeName: String
        get() = ""
    open val id: String
        get() = ""
    open val idLike: String
        get() = ""
    open val homeUrl: String
        get() = ""
    open val supportUrl: String
        get() = ""
    open val bugReportUrl: String
        get() = ""
    open val privacyPolicyUrl: String
        get() = ""
    open val codename: String
        get() = ""
    open val logo: String
        get() = ""
    open val machineName: String
        get() = ""
}