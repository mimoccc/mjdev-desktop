package eu.mjdev.desktop.helpers.system

import java.io.File

@Suppress("unused")
class OsRelease(
    configFile: File = File("/etc/os-release"),
    configFileContent: List<String> = configFile.readLines().filter {
        (!it.startsWith("#")) && (!it.trim().isEmpty())
    }
) : HashMap<String, File>() {
    init {
        configFileContent.map {
            it.split("=").let { Pair(it[0], it.getOrNull(1)) }
        }.forEach {
            put(it.first, File(it.second ?: ""))
        }
    }

    val prettyName
        get() = this[PRETTY_NAME]
    val name
        get() = this[NAME]
    val versionId
        get() = this[VERSION_ID]
    val version
        get() = this[VERSION]
    val versionCodeName
        get() = this[VERSION_CODENAME]
    val id
        get() = this[ID]
    val idLike
        get() = this[ID_LIKE]
    val homeUrl
        get() = this[HOME_URL]
    val supportUrl
        get() = this[SUPPORT_URL]
    val bugReportUrl
        get() = this[BUG_REPORT_URL]
    val privacyPolicyUrl
        get() = this[PRIVACY_POLICY_URL]
    val ubuntuCodename
        get() = this[UBUNTU_CODENAME]
    val logo
        get() = this[LOGO]

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