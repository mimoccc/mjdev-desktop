import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.MessageDigest

/**
 * Turns the jpackage desktop `.deb` (which only carries the Compose app + its Java/X deps) into a
 * *complete, self-installable* psippr-desktop package, in place:
 *
 *  - injects the wayland compositor (`psipprc`), the session launcher (`psippr-session`) and the
 *    `wayland-sessions` entry, so one deb is the whole desktop, not just the app;
 *  - rewrites `Depends`: strips the ubuntu-only `libjpeg-turbo8` (absent on Debian; Skiko bundles
 *    its codecs) and appends the wayland runtime stack, so `apt install ./psippr-desktop.deb` on a
 *    clean console Linux pulls libwlroots/xwayland/mesa/… and the desktop actually starts (plain
 *    `dpkg -i` can't resolve deps — that is why fresh installs black-screened);
 *  - adds `Recommends: seatd`, a postinst (drop the stale X11 session, enable seatd, ldconfig),
 *    regenerates `md5sums`, and rebuilds root:root.
 *
 * jpackage's control ends with a trailing blank line, so fields are edited mid-stanza (appending
 * at EOF would start a second stanza -> dpkg "multiple package info entries"). Mirrors the style of
 * [PackageAppImageTask]; config-cache safe (only String/list inputs, no project references). If
 * `dpkg-deb` is unavailable the task logs and skips — it never fails the build.
 */
abstract class PackageFullDebTask : DefaultTask() {
    @get:Input abstract val debDir: Property<String>        // dir holding the jpackage .deb (overwritten in place)
    @get:Input abstract val sessionDir: Property<String>    // stageSession output: psipprc + psippr-session + psippr.desktop
    @get:Input abstract val runtimeDepends: ListProperty<String> // wayland runtime stack to add to Depends
    @get:Input abstract val greeterDepends: ListProperty<String> // login screen display manager (greetd)
    @get:Input abstract val greeterPackage: Property<String>     // the login screen package, suggested
    @get:Input abstract val legacyPackages: ListProperty<String> // old package names: Replaces/Conflicts/Provides
    @get:Input abstract val legacyFiles: ListProperty<String>    // files of the old name installed outside the package

    @TaskAction
    fun build() {
        val dpkgDeb = which("dpkg-deb")
        if (dpkgDeb == null) {
            logger.lifecycle("[full-deb] dpkg-deb not found on PATH — skipping (deb keeps jpackage deps only). apt install dpkg-dev")
            return
        }
        val dir = File(debDir.get())
        val deb = dir.listFiles { f -> f.extension == "deb" }?.firstOrNull()
        if (deb == null) { logger.lifecycle("[full-deb] no .deb in $dir — skipping"); return }
        val sess = File(sessionDir.get())
        val psipprc = File(sess, "psipprc")
        check(psipprc.canExecute()) { "[full-deb] psipprc not found in $sess (run :compositor:stageSession)" }

        val work = File.createTempFile("psippr-fulldeb", "").apply { delete(); mkdirs() }
        try {
            run(dpkgDeb, "-R", deb.absolutePath, work.absolutePath)

            // the whole desktop in one package
            psipprc.copyExec(File(work, "usr/bin/psipprc"))
            File(sess, "psippr-session").copyExec(File(work, "usr/bin/psippr-session"))
            File(sess, "psippr.desktop").copyInto(File(work, "usr/share/wayland-sessions/psippr.desktop"), 420)
            // old: the login screen (greetd greeter, its config and the display manager choice) shipped
            // here; it is the psippr-greeter deb now (PackageGreeterDebTask)
//            File(sess, "psippr-greeter").copyExec(File(work, "usr/bin/psippr-greeter"))
//            File(sess, "psippr-greetd.toml").copyInto(File(work, "etc/psippr-desktop/greetd.toml"), 420)
//            File(sess, "psippr-greetd.conf").copyInto(File(work, "etc/systemd/system/greetd.service.d/psippr.conf"), 420)
//            File(sess, "debian/templates").copyInto(File(work, "DEBIAN/templates"), 420)
//            File(sess, "debian/config").copyExec(File(work, "DEBIAN/config"))
            // the desktop app on PATH (jpackage installs it only under /opt)
            linkLauncher(work)

            rewriteControl(File(work, "DEBIAN/control"))
            // old: no display manager choice
//            writePostinst(File(work, "DEBIAN/postinst"))
            // old: display manager choice, moved to the psippr-greeter deb
//            writePostinst(File(work, "DEBIAN/postinst"), File(sess, "debian/postinst-dm").readText())
            writePostinst(File(work, "DEBIAN/postinst"), "")
            regenMd5sums(work)

            val tmpOut = File(dir, deb.name + ".full")
            run(dpkgDeb, "--root-owner-group", "--build", "-Zxz", work.absolutePath, tmpOut.absolutePath)
            check(tmpOut.exists() && deb.delete() && tmpOut.renameTo(deb)) { "[full-deb] could not overwrite ${deb.name}" }
            logger.lifecycle("[full-deb] ${deb.name} now carries the compositor + wayland Depends (self-installable)")
        } finally {
            work.deleteRecursively()
        }
    }

    /** Strip libjpeg-turbo8, append the runtime stack to Depends (mid-stanza), add Recommends: seatd. */
    private fun rewriteControl(ctrl: File) {
        // old: wayland runtime stack only
//        val deps = runtimeDepends.get()
        // old: plus greetd for the login screen, which is the psippr-greeter deb now
//        val deps = runtimeDepends.get() + greeterDepends.get().filter { it.isNotBlank() }
        val deps = runtimeDepends.get()
        if (deps.isEmpty()) return
        val lines = ctrl.readLines().toMutableList()
        var dependsIdx = lines.indexOfFirst { it.startsWith("Depends:") }
        if (dependsIdx >= 0) {
            val existing = lines[dependsIdx].removePrefix("Depends:")
                .split(",").map { it.trim() }.filter { it.isNotEmpty() && it != "libjpeg-turbo8" }
            lines[dependsIdx] = "Depends: " + (existing + deps).joinToString(", ")
        } else {
            val pkgIdx = lines.indexOfFirst { it.startsWith("Package:") }.coerceAtLeast(0)
            lines.add(pkgIdx + 1, "Depends: " + deps.joinToString(", "))
            dependsIdx = pkgIdx + 1
        }
        if (lines.none { it.startsWith("Recommends:") }) lines.add(dependsIdx + 1, "Recommends: seatd")
        // upgrade from the old name: dpkg removes the old package in favour of this one
        val legacy = legacyPackages.get().filter { it.isNotBlank() }
        if (legacy.isNotEmpty()) {
            val names = legacy.joinToString(", ")
            LEGACY_FIELDS.forEach { field ->
                // jpackage may already write the field (Provides): extend it, a second one is invalid
                val idx = lines.indexOfFirst { it.startsWith("$field:") }
                if (idx >= 0) lines[idx] = lines[idx] + ", " + names else lines.add(dependsIdx + 1, "$field: $names")
            }
        }
        // old: a display manager, listed in the "Default display manager" choice - psippr-greeter is now
//        val provides = lines.indexOfFirst { it.startsWith("Provides:") }
//        if (provides >= 0) lines[provides] = lines[provides] + ", " + DM_PROVIDES else lines.add(dependsIdx + 1, "Provides: $DM_PROVIDES")
        // the login screen package, offered but not forced (a gdm / lightdm user keeps theirs)
        if (lines.none { it.startsWith("Suggests:") }) lines.add(dependsIdx + 1, "Suggests: ${greeterPackage.get()}")
        ctrl.writeText(lines.joinToString("\n") + "\n")
    }

    private fun writePostinst(
        postinst: File,
        dmChoice: String,
    ) {
        postinst.writeText(
            """
            #!/bin/sh
            set -e
            if [ "${'$'}1" = configure ]; then
                rm -f /usr/share/xsessions/psippr-desktop.desktop \
                      /usr/share/gnome-session/sessions/psippr-desktop.session 2>/dev/null || true
                rm -f ${legacyFiles.get().joinToString(" ")} 2>/dev/null || true
                systemctl enable seatd 2>/dev/null || true
                ldconfig 2>/dev/null || true
            fi
            """.trimIndent() + "\n" + dmChoice + "exit 0\n",
        )
        postinst.setExecutable(true, false)
    }

    /** `/usr/bin/<app>` -> `/opt/<app>/bin/<app>`, so the desktop app starts by name from a terminal. */
    private fun linkLauncher(work: File) {
        val app = File(work, "opt").listFiles()?.firstOrNull { File(it, "bin/${it.name}").exists() } ?: return
        val link = File(work, "usr/bin/${app.name}").toPath()
        java.nio.file.Files.createDirectories(link.parent)
        java.nio.file.Files.deleteIfExists(link)
        java.nio.file.Files.createSymbolicLink(link, java.nio.file.Paths.get("/opt/${app.name}/bin/${app.name}"))
    }

    /** Regenerate DEBIAN/md5sums for everything we added (paths relative to root, no leading ./). */
    private fun regenMd5sums(work: File) {
        val sb = StringBuilder()
        // symlinks are not hashed (dpkg lists regular files only; isFile would follow the link to the host)
        work.walkTopDown().filter {
            it.isFile && !java.nio.file.Files.isSymbolicLink(it.toPath()) && !it.path.startsWith(File(work, "DEBIAN").path)
        }.forEach { f ->
            val md = MessageDigest.getInstance("MD5").digest(f.readBytes())
                .joinToString("") { "%02x".format(it) }
            sb.append(md).append("  ").append(f.relativeTo(work).path).append('\n')
        }
        File(work, "DEBIAN/md5sums").writeText(sb.toString())
    }

    private fun File.copyExec(dest: File) { dest.parentFile.mkdirs(); copyTo(dest, overwrite = true); dest.setExecutable(true, false) }
    private fun File.copyInto(dest: File, @Suppress("UNUSED_PARAMETER") mode: Int) { dest.parentFile.mkdirs(); copyTo(dest, overwrite = true) }

    private fun run(vararg cmd: String) {
        val p = ProcessBuilder(*cmd).redirectErrorStream(true).start()
        val log = p.inputStream.bufferedReader().readText()
        check(p.waitFor() == 0) { "[full-deb] command failed: ${cmd.joinToString(" ")}\n${log.takeLast(800)}" }
    }

    private fun which(bin: String): String? =
        ((System.getenv("PATH") ?: "").split(File.pathSeparator) + listOf("/usr/bin", "/bin"))
            .map { File(it, bin) }.firstOrNull { it.canExecute() }?.absolutePath

    private companion object {
        /** Control fields naming the old package, so an upgrade replaces it in place. */
        val LEGACY_FIELDS = listOf("Provides", "Conflicts", "Replaces")

        /** Virtual package of display managers (gdm3, lightdm, sddm provide it too). */
        const val DM_PROVIDES = "x-display-manager"
    }
}
