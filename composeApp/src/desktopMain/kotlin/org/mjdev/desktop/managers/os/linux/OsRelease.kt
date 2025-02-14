/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.managers.os.linux

import okio.Path
import okio.Path.Companion.toPath
import org.mjdev.desktop.extensions.PathExt.lines
import org.mjdev.desktop.extensions.Text.notStartsWith


@Suppress("unused")
class OsRelease(
    configFile: Path = "/etc/os-release".toPath(),
    configFileContent: List<String> = configFile.lines.filter { line ->
        line.trim().let { l -> l.isNotEmpty() && l.notStartsWith("#") }
    }
) : HashMap<String, String>() {
    init {
        configFileContent.map {
            it.split("=").let { pair -> Pair(pair[0], pair.getOrNull(1)) }
        }.forEach {
            put(it.first, it.second.orEmpty())
        }
    }

    val prettyName: String
        get() = this[PRETTY_NAME].orEmpty()
    val name: String
        get() = this[NAME].orEmpty()
    val versionId: String
        get() = this[VERSION_ID].orEmpty()
    val version: String
        get() = this[VERSION].orEmpty()
    val versionCodeName: String
        get() = this[VERSION_CODENAME].orEmpty()
    val id: String
        get() = this[ID].orEmpty()
    val idLike: String
        get() = this[ID_LIKE].orEmpty()
    val homeUrl: String
        get() = this[HOME_URL].orEmpty()
    val supportUrl: String
        get() = this[SUPPORT_URL].orEmpty()
    val bugReportUrl: String
        get() = this[BUG_REPORT_URL].orEmpty()
    val privacyPolicyUrl: String
        get() = this[PRIVACY_POLICY_URL].orEmpty()
    val ubuntuCodename: String
        get() = this[UBUNTU_CODENAME].orEmpty()
    val logo: String
        get() = this[LOGO].orEmpty()

    companion object {
        const val PRETTY_NAME = "PRETTY_NAME"
        const val NAME = "NAME"
        const val VERSION_ID = "VERSION_ID"
        const val VERSION = "VERSION"
        const val VERSION_CODENAME = "VERSION_CODENAME"
        const val ID = "ID"
        const val ID_LIKE = "ID_LIKE"
        const val HOME_URL = "HOME_URL"
        const val SUPPORT_URL = "SUPPORT_URL"
        const val BUG_REPORT_URL = "BUG_REPORT_URL"
        const val PRIVACY_POLICY_URL = "PRIVACY_POLICY_URL"
        const val UBUNTU_CODENAME = "UBUNTU_CODENAME"
        const val LOGO = "LOGO"
    }
}
