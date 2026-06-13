import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Base64

/**
 * Wraps a jpackage app-image (a directory tree) into a single, CLI-runnable
 * `.AppImage` file using `appimagetool`.
 *
 * Compose Multiplatform's `TargetFormat.AppImage` only produces the jpackage
 * app-image *directory* (`app/<name>/bin/<name>` + `lib/`), not a real AppImage.
 * This task builds a proper AppDir (AppRun + .desktop + icon) around it and runs
 * `appimagetool`. If `appimagetool` is not on PATH the task logs how to get it
 * (no root needed) and skips — it never fails the build.
 *
 * Config-cache safe: only String inputs, no Gradle script/project references.
 */
abstract class PackageAppImageTask : DefaultTask() {
    @get:Input abstract val appName: Property<String>
    @get:Input abstract val appImagePath: Property<String> // jpackage app/<name> directory
    @get:Input abstract val outputPath: Property<String>    // target single-file .AppImage

    @get:Input @get:org.gradle.api.tasks.Optional
    abstract val toolPath: Property<String> // explicit appimagetool (e.g. downloaded one); falls back to PATH

    @TaskAction
    fun build() {
        val tool = toolPath.orNull?.let { File(it) }?.takeIf { it.canExecute() }?.absolutePath
            ?: which("appimagetool")
        val src = File(appImagePath.get())
        val name = appName.get()
        val out = File(outputPath.get())

        if (tool == null) {
            logger.lifecycle(
                "[appimage] appimagetool not found on PATH — skipping single-file .AppImage.\n" +
                    "           Install without root:\n" +
                    "             curl -L -o ~/.local/bin/appimagetool " +
                    "https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage\n" +
                    "             chmod +x ~/.local/bin/appimagetool   (then re-run buildAll)",
            )
            return
        }
        if (!src.isDirectory) { logger.lifecycle("[appimage] jpackage app-image not found at $src — skipping"); return }

        out.parentFile.mkdirs()
        val appDir = File(out.parentFile, "AppDir").apply { deleteRecursively(); mkdirs() }

        // payload (bin/, lib/, …)
        src.copyRecursively(appDir, overwrite = true)

        // AppRun launcher -> bin/<name>
        File(appDir, "AppRun").apply {
            writeText(
                """
                #!/bin/sh
                HERE="${'$'}(dirname "${'$'}(readlink -f "${'$'}0")")"
                exec "${'$'}HERE/bin/$name" "${'$'}@"
                """.trimIndent() + "\n",
            )
            setExecutable(true)
        }

        // desktop entry
        File(appDir, "$name.desktop").writeText(
            "[Desktop Entry]\nType=Application\nName=$name\nExec=$name\nIcon=$name\nCategories=Utility;\nTerminal=false\n",
        )

        // icon: reuse jpackage icon if present, else a 1x1 transparent placeholder
        val icon = File(appDir, "$name.png")
        val jpackageIcon = File(src, "lib/$name.png")
        if (jpackageIcon.exists()) jpackageIcon.copyTo(icon, overwrite = true)
        else icon.writeBytes(
            Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==",
            ),
        )

        out.delete()
        // appimagetool is itself an AppImage; run it without FUSE via extract-and-run
        val cmd = buildList {
            add(tool)
            if (tool.endsWith(".AppImage")) add("--appimage-extract-and-run")
            add(appDir.absolutePath)
            add(out.absolutePath)
        }
        val proc = ProcessBuilder(cmd)
            .apply { environment()["ARCH"] = "x86_64" }
            .redirectErrorStream(true)
            .start()
        val log = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        appDir.deleteRecursively()
        check(code == 0) { "[appimage] appimagetool failed (exit $code):\n${log.takeLast(1000)}" }
        logger.lifecycle("[appimage] created ${out.name} (${out.length() / (1024 * 1024)} MB)")
    }

    private fun which(bin: String): String? =
        (System.getenv("PATH") ?: "").split(File.pathSeparator)
            .map { File(it, bin) }.firstOrNull { it.canExecute() }?.absolutePath
}
