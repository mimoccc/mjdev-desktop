package org.mjdev.desktop.components.controlcenter.pages.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import org.mjdev.desktop.components.controlcenter.base.ControlCenterPage
import org.mjdev.desktop.components.controlcenter.settings.SettingsInfoRow
import org.mjdev.desktop.components.controlcenter.settings.SettingsPageColumn
import org.mjdev.desktop.components.controlcenter.settings.SettingsSection
import org.mjdev.desktop.context.IDesktopContext
import org.mjdev.desktop.extensions.Compose.preview
import org.mjdev.desktop.extensions.DoubleExt.toMemorySizeReadable
import org.mjdev.desktop.helpers.system.meminfo.MemInfo
import org.mjdev.desktop.icons.custom.Mjdev

/** Static labels used by [AboutPage]. */
private object AboutLabels {
    const val SYSTEM = "System"
    const val MACHINE = "Machine"
    const val USER = "User"
    const val HOME = "Home"
    const val SHELL = "Shell"
    const val DISTRIBUTION = "Distribution"
    const val KERNEL = "Kernel"
    const val UPTIME = "Uptime"
    const val MEMORY = "Memory"
    const val SUPPORT = "Support"
    const val HOME_PAGE = "Home page"
    const val LOCALE = "Locale"
}

/** Control center page with basic information about this machine and the current user. */
@Suppress("FunctionName")
fun AboutPage(context: IDesktopContext) = ControlCenterPage(
    context = context,
    icon = Mjdev,
    name = "About",
    condition = { true },
) {
    SettingsPageColumn {
        SettingsSection(title = AboutLabels.SYSTEM) {
            // Read once per page open: these values come from files and shell commands.
            val os = context.osManager
            val machine = remember { context.machineName }
            val distribution = remember { os.prettyName }
            val kernel = remember { os.kernel }
            val uptime = remember { os.uptime }
            val memory = remember { MemInfo(context).total.toMemorySizeReadable() }
            SettingsInfoRow(AboutLabels.MACHINE, machine)
            SettingsInfoRow(AboutLabels.DISTRIBUTION, distribution)
            SettingsInfoRow(AboutLabels.KERNEL, kernel)
            SettingsInfoRow(AboutLabels.UPTIME, uptime)
            SettingsInfoRow(AboutLabels.MEMORY, memory)
            SettingsInfoRow(AboutLabels.HOME_PAGE, remember { os.homeUrl })
            SettingsInfoRow(AboutLabels.SUPPORT, remember { os.supportUrl })
            SettingsInfoRow(AboutLabels.LOCALE, context.currentLocale.toString())
        }
        SettingsSection(title = AboutLabels.USER) {
            SettingsInfoRow(AboutLabels.USER, currentUser.userName)
            SettingsInfoRow(AboutLabels.HOME, currentUser.home)
            SettingsInfoRow(AboutLabels.SHELL, currentUser.shell)
        }
    }
}

@Preview
@Composable
fun PreviewAboutPage() = preview {
    AboutPage(context).Render()
}
