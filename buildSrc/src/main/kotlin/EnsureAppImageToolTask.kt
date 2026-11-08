import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Ensures `appimagetool` is available before the AppImage packaging step.
 * If it is already on PATH or already downloaded at [toolPath], it does nothing;
 * otherwise it downloads the continuous release (no root required) and marks it
 * executable. Stored under .gradle/ so it survives `clean`.
 *
 * Config-cache safe: only String inputs, no Gradle script/project references.
 */
abstract class EnsureAppImageToolTask : DefaultTask() {
    @get:Input abstract val toolPath: Property<String>
    @get:Input @get:Optional abstract val url: Property<String>

    @TaskAction
    fun ensure() {
        if (which("appimagetool") != null) { logger.lifecycle("[appimagetool] found on PATH — no download needed"); return }
        val target = File(toolPath.get())
        if (target.canExecute() && target.length() > 0) { logger.lifecycle("[appimagetool] already downloaded: $target"); return }

        val src = url.orNull
            ?: "https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage"
        target.parentFile.mkdirs()
        logger.lifecycle("[appimagetool] downloading $src")
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build()
        val resp = client.send(
            HttpRequest.newBuilder(URI.create(src)).timeout(Duration.ofMinutes(5)).GET().build(),
            HttpResponse.BodyHandlers.ofFile(target.toPath()),
        )
        check(resp.statusCode() in 200..299) { "[appimagetool] download failed: HTTP ${resp.statusCode()}" }
        target.setExecutable(true)
        logger.lifecycle("[appimagetool] downloaded -> $target (${target.length() / (1024 * 1024)} MB)")
    }

    private fun which(bin: String): String? =
        (System.getenv("PATH") ?: "").split(File.pathSeparator)
            .map { File(it, bin) }.firstOrNull { it.canExecute() }?.absolutePath
}
