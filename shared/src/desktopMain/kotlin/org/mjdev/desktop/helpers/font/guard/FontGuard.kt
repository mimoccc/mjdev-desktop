package org.mjdev.desktop.helpers.font.guard

import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.AppCacheDirName
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.CacheDirName
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.FontconfigFileEnv
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.GuardConfigFileName
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.GuardMarkerEnv
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.LinuxOsName
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.OsNameProperty
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.SystemFontconfigFile
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.UserHomeProperty
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.XdgCacheHomeEnv
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * Keeps the desktop from freezing at start on machines whose fontconfig lists fonts without a
 * family name (Skia spins forever on them, see [BrokenFontScanner]). Must be the first call of
 * `main`, before Compose touches any font.
 *
 * - nothing broken found (the normal case): returns at once, the app starts as usual
 * - broken fonts found: writes a fontconfig file hiding them, runs the app again with it and
 *   exits with the exit code of that run
 * - any problem (no fc-list, no Linux, unknown command line, ...): returns, never worse than before
 *
 * Fonts and the fontconfig cache of the user are never changed.
 */
object FontGuard {
    /**
     * Protects the start of the app; returns only when the app should continue in this process.
     *
     * @param args arguments of `main`, passed on to the relaunched process
     */
    fun protect(args: List<String>) {
        if (!isNeeded()) return
        val broken = BrokenFontScanner.scan()
        if (broken.isEmpty()) return
        val command = FontGuardRelauncher.currentCommand(args) ?: return
        val guardConfig = runCatching { writeGuardConfig(broken) }.getOrNull() ?: return
        System.err.println("mjdev font guard: hiding ${broken.size} fonts without family, config: $guardConfig")
        exitProcess(FontGuardRelauncher.relaunch(command, guardConfig))
    }

    // only on Linux (fontconfig) and not in the already relaunched process
    private fun isNeeded(): Boolean = isLinux() && System.getenv(GuardMarkerEnv.value) == null

    private fun isLinux(): Boolean = System
        .getProperty(OsNameProperty.value)
        .orEmpty()
        .lowercase()
        .contains(LinuxOsName.value)

    private fun writeGuardConfig(broken: List<String>): Path {
        val file = cacheDir().resolve(GuardConfigFileName.value)
        Files.createDirectories(file.parent)
        Files.writeString(file, FontGuardConfig.render(baseConfig(file), broken))
        return file
    }

    // the configuration fontconfig would use without the guard (never the guard file itself,
    // which would include itself)
    private fun baseConfig(guardFile: Path): String = System
        .getenv(FontconfigFileEnv.value)
        ?.takeIf { env -> env.isNotBlank() && Paths.get(env).toAbsolutePath() != guardFile.toAbsolutePath() }
        ?: SystemFontconfigFile.value

    private fun cacheDir(): Path {
        val cacheHome =
            System.getenv(XdgCacheHomeEnv.value)?.takeIf { it.isNotBlank() }?.let { Paths.get(it) }
                ?: Paths.get(System.getProperty(UserHomeProperty.value), CacheDirName.value)
        return cacheHome.resolve(AppCacheDirName.value)
    }
}
