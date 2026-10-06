package org.mjdev.desktop.helpers.font.guard

import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.FcListCommand
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.FcListFormat
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.FcListFormatOption
import org.mjdev.desktop.helpers.font.guard.FontGuardSetting.FcListSeparator
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Finds fonts that fontconfig lists without a family name. Skia (used by Compose) spins forever
 * in its font manager initialisation when it meets such an entry, freezing the whole desktop
 * before the first frame - seen with stale cache entries of `.woff` / `.woff2` web fonts.
 */
object BrokenFontScanner {
    /** Maximum time [FcListCommand] may run; a slower fontconfig is treated as "nothing found". */
    private const val FC_LIST_TIMEOUT_SECONDS = 10L

    /** Files of all fonts listed without a family name, or empty when fontconfig is not usable. */
    fun scan(): List<String> = runCatching {
        val process =
            ProcessBuilder(FcListCommand.value, FcListFormatOption.value, FcListFormat.value)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
        // read on another thread: reading first would block forever if fc-list hung
        val output = CompletableFuture.supplyAsync { process.inputStream.bufferedReader().readText() }
        if (process.waitFor(FC_LIST_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            parse(output.get(FC_LIST_TIMEOUT_SECONDS, TimeUnit.SECONDS))
        } else {
            process.destroyForcibly()
            emptyList()
        }
    }.getOrDefault(emptyList())

    /** Files of the family-less fonts in [output] of [FcListCommand] with [FcListFormat], without duplicates. */
    fun parse(output: String): List<String> = output
        .lineSequence()
        .filter { line -> line.contains(FcListSeparator.value) }
        .map { line -> line.split(FcListSeparator.value, limit = 2) }
        .filter { (family, file) -> family.isBlank() && file.isNotBlank() }
        .map { (_, file) -> file }
        .distinct()
        .toList()
}
