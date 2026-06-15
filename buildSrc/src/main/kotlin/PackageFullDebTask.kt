import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.MessageDigest

/**
 * Turns the jpackage desktop `.deb` (which only carries the Compose app + its Java/X deps) into a
 * *complete, self-installable* mjdev-desktop package, in place:
 *
 *  - injects the wayland compositor (`mjdevc`), the session launcher (`mjdev-session`) and the
 *    `wayland-sessions` entry, so one deb is the whole desktop, not just the app;
 *  - rewrites `Depends`: strips the ubuntu-only `libjpeg-turbo8` (absent on Debian; Skiko bundles
 *    its codecs) and appends the wayland runtime stack, so `apt install ./mjdev-desktop.deb` on a
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
    @get:Input abstract val sessionDir: Property<String>    // stageSession output: mjdevc + mjdev-session + mjdev.desktop
    @get:Input abstract val runtimeDepends: ListProperty<String> // wayland runtime stack to add to Depends

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
        val mjdevc = File(sess, "mjdevc")
        check(mjdevc.canExecute()) { "[full-deb] mjdevc not found in $sess (run :compositor:stageSession)" }

        val work = File.createTempFile("mjdev-fulldeb", "").apply { delete(); mkdirs() }
        try {
            run(dpkgDeb, "-R", deb.absolutePath, work.absolutePath)

            // the whole desktop in one package
            mjdevc.copyExec(File(work, "usr/bin/mjdevc"))
            File(sess, "mjdev-session").copyExec(File(work, "usr/bin/mjdev-session"))
            File(sess, "mjdev.desktop").copyInto(File(work, "usr/share/wayland-sessions/mjdev.desktop"), 420)

            rewriteControl(File(work, "DEBIAN/control"))
            writePostinst(File(work, "DEBIAN/postinst"))
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
        ctrl.writeText(lines.joinToString("\n") + "\n")
    }

    private fun writePostinst(postinst: File) {
        postinst.writeText(
            """
            #!/bin/sh
            set -e
            if [ "${'$'}1" = configure ]; then
                rm -f /usr/share/xsessions/mjdev-desktop.desktop \
                      /usr/share/gnome-session/sessions/mjdev-desktop.session 2>/dev/null || true
                systemctl enable seatd 2>/dev/null || true
                ldconfig 2>/dev/null || true
            fi
            exit 0
            """.trimIndent() + "\n",
        )
        postinst.setExecutable(true, false)
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
}
