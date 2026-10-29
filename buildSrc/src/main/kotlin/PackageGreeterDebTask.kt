import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.MessageDigest

/**
 * Turns the jpackage greeter `.deb` (the Compose login screen app only) into the installable
 * psippr-greeter display manager package, in place:
 *
 *  - adds the launcher greetd runs (`/usr/bin/psippr-greeter`: psipprc with the greeter app as its
 *    only client), the greetd config and the greetd.service drop-in that points greetd at it;
 *  - adds the usual debconf "Default display manager" question (shared with gdm3 / lightdm / sddm)
 *    and a postinst that switches the display manager when psippr-greeter is chosen;
 *  - rewrites the control file: Depends on greetd and the desktop package (it ships psipprc),
 *    Provides x-display-manager, Replaces/Breaks the desktop versions that still carried the greeter.
 *
 * Config-cache safe (only String/list inputs). If `dpkg-deb` is unavailable the task logs and skips.
 */
abstract class PackageGreeterDebTask : DefaultTask() {
    /** Dir holding the jpackage greeter .deb (overwritten in place). */
    @get:Input abstract val debDir: Property<String>

    /** Dir with the greeter session files (session/greeter in the repo). */
    @get:Input abstract val sessionDir: Property<String>

    /** Display manager daemon packages the greeter needs (greetd). */
    @get:Input abstract val greeterDepends: ListProperty<String>

    /** The desktop package: provides psipprc, which hosts the greeter window. */
    @get:Input abstract val desktopPackage: Property<String>

    /** First desktop package version without the greeter files (they moved into this package). */
    @get:Input abstract val desktopSplitVersion: Property<String>

    @TaskAction
    fun build() {
        val dpkgDeb = which(DPKG_DEB)
        if (dpkgDeb == null) {
            logger.lifecycle("[greeter-deb] dpkg-deb not found on PATH — skipping. apt install dpkg-dev")
            return
        }
        val dir = File(debDir.get())
        val deb = dir.listFiles { f -> f.extension == DEB_EXTENSION }?.firstOrNull()
        if (deb == null) {
            logger.lifecycle("[greeter-deb] no .deb in $dir — skipping")
            return
        }
        val sess = File(sessionDir.get())
        val work = File.createTempFile(WORK_PREFIX, "").apply { delete(); mkdirs() }
        try {
            run(dpkgDeb, "-R", deb.absolutePath, work.absolutePath)
            addSessionFiles(sess, work)
            rewriteControl(File(work, "DEBIAN/control"))
            writePostinst(File(work, "DEBIAN/postinst"), File(sess, "postinst-dm").readText())
            relaxMenuUninstall(File(work, "DEBIAN/prerm"))
            regenMd5sums(work)
            val tmpOut = File(dir, deb.name + TMP_SUFFIX)
            run(dpkgDeb, "--root-owner-group", "--build", "-Zxz", work.absolutePath, tmpOut.absolutePath)
            check(tmpOut.exists() && deb.delete() && tmpOut.renameTo(deb)) { "[greeter-deb] could not overwrite ${deb.name}" }
            logger.lifecycle("[greeter-deb] ${deb.name} is now the psippr-greeter display manager package")
        } finally {
            work.deleteRecursively()
        }
    }

    /** Launcher, greetd config + drop-in and the debconf display manager question. */
    private fun addSessionFiles(
        sess: File,
        work: File,
    ) {
        File(sess, "psippr-greeter").copyExec(File(work, "usr/bin/psippr-greeter"))
        File(sess, "psippr-greetd.toml").copyInto(File(work, "etc/psippr-greeter/greetd.toml"))
        File(sess, "psippr-greetd.conf").copyInto(File(work, "etc/systemd/system/greetd.service.d/psippr.conf"))
        File(sess, "templates").copyInto(File(work, "DEBIAN/templates"))
        File(sess, "config").copyExec(File(work, "DEBIAN/config"))
    }

    /** Depends greetd + desktop, Provides x-display-manager, takes the greeter files over from old desktops. */
    private fun rewriteControl(ctrl: File) {
        val lines = ctrl.readLines().toMutableList()
        val deps = greeterDepends.get().filter { it.isNotBlank() } + desktopPackage.get()
        val dependsIdx = lines.indexOfFirst { it.startsWith("Depends:") }
        if (dependsIdx >= 0) {
            val existing = lines[dependsIdx].removePrefix("Depends:")
                .split(",").map { it.trim() }.filter { it.isNotEmpty() && it != SPURIOUS_DEPEND }
            // distinct: a second run on an already repacked deb must not repeat entries
            lines[dependsIdx] = "Depends: " + (existing + deps).distinct().joinToString(", ")
        } else {
            val pkgIdx = lines.indexOfFirst { it.startsWith("Package:") }.coerceAtLeast(0)
            lines.add(pkgIdx + 1, "Depends: " + deps.joinToString(", "))
        }
        val anchor = lines.indexOfFirst { it.startsWith("Depends:") } + 1
        val older = "${desktopPackage.get()} (<< ${desktopSplitVersion.get()})"
        appendField(lines, anchor, "Provides", DM_PROVIDES)
        appendField(lines, anchor, "Replaces", older)
        appendField(lines, anchor, "Breaks", older)
        ctrl.writeText(lines.joinToString("\n") + "\n")
    }

    // extends a field jpackage may already write (a second one is invalid), else adds it mid-stanza
    private fun appendField(
        lines: MutableList<String>,
        anchor: Int,
        field: String,
        value: String,
    ) {
        val idx = lines.indexOfFirst { it.startsWith("$field:") }
        when {
            idx < 0 -> lines.add(anchor, "$field: $value")
            // already there from an earlier run on the same deb
            lines[idx].removePrefix("$field:").split(",").any { it.trim() == value } -> Unit
            else -> lines[idx] = lines[idx] + ", " + value
        }
    }

    // jpackage's postinst would add a menu entry for the greeter app: replaced by the display manager choice
    private fun writePostinst(
        postinst: File,
        dmChoice: String,
    ) {
        postinst.writeText("#!/bin/sh\nset -e\n" + dmChoice + "exit 0\n")
        postinst.setExecutable(true, false)
    }

    // the greeter never installs jpackage's menu entry, so its uninstall in prerm must not fail removal
    private fun relaxMenuUninstall(prerm: File) {
        if (!prerm.exists()) return
        prerm.writeText(
            prerm.readLines().joinToString("\n", postfix = "\n") { line ->
                if (line.trim().startsWith(MENU_UNINSTALL) && !line.contains(OR_TRUE)) "$line $OR_TRUE" else line
            },
        )
        prerm.setExecutable(true, false)
    }

    /** Regenerate DEBIAN/md5sums for everything we added (paths relative to root, no leading ./). */
    private fun regenMd5sums(work: File) {
        val sb = StringBuilder()
        work.walkTopDown().filter { it.isFile && !it.path.startsWith(File(work, "DEBIAN").path) }.forEach { f ->
            val md = MessageDigest.getInstance("MD5").digest(f.readBytes())
                .joinToString("") { "%02x".format(it) }
            sb.append(md).append("  ").append(f.relativeTo(work).path).append('\n')
        }
        File(work, "DEBIAN/md5sums").writeText(sb.toString())
    }

    private fun File.copyExec(dest: File) {
        dest.parentFile.mkdirs()
        copyTo(dest, overwrite = true)
        dest.setExecutable(true, false)
    }

    private fun File.copyInto(dest: File) {
        dest.parentFile.mkdirs()
        copyTo(dest, overwrite = true)
    }

    private fun run(vararg cmd: String) {
        val p = ProcessBuilder(*cmd).redirectErrorStream(true).start()
        val log = p.inputStream.bufferedReader().readText()
        check(p.waitFor() == 0) { "[greeter-deb] command failed: ${cmd.joinToString(" ")}\n${log.takeLast(LOG_TAIL)}" }
    }

    private fun which(bin: String): String? =
        ((System.getenv("PATH") ?: "").split(File.pathSeparator) + listOf("/usr/bin", "/bin"))
            .map { File(it, bin) }.firstOrNull { it.canExecute() }?.absolutePath

    private companion object {
        /** Virtual package of display managers (gdm3, lightdm, sddm provide it too). */
        const val DM_PROVIDES = "x-display-manager"

        /** Ubuntu-only jpackage dependency, absent on Debian (Skiko bundles its codecs). */
        const val SPURIOUS_DEPEND = "libjpeg-turbo8"

        /** Debian package builder. */
        const val DPKG_DEB = "dpkg-deb"

        /** Extension of the jpackage output. */
        const val DEB_EXTENSION = "deb"

        /** Prefix of the temporary unpack dir. */
        const val WORK_PREFIX = "psippr-greeterdeb"

        /** Suffix of the rebuilt deb before it replaces the original. */
        const val TMP_SUFFIX = ".greeter"

        /** jpackage's prerm call that removes the app menu entry. */
        const val MENU_UNINSTALL = "xdg-desktop-menu uninstall"

        /** Shell suffix that makes a command non-fatal under `set -e`. */
        const val OR_TRUE = "|| true"

        /** Characters of a failed command's output kept in the error. */
        const val LOG_TAIL = 800
    }
}
