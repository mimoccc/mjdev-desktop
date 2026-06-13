package ai

import java.io.File

/**
 * Non-invasive dependency auto-update, driven by the Ben Manes report at
 * `reports/dependencies/dependency-updates.txt`:
 *
 *  - applies only STABLE updates (rejects alpha/beta/rc/dev/snapshot/tethys/…),
 *  - never touches pinned keys (Kotlin / Compose / AGP / IntelliJ — coordinated upgrades),
 *  - after applying, runs [verifyTask]; if the build fails it reverts (all-at-once first,
 *    then bisects per-library, keeping only the bumps that still build).
 *
 * Mutates only `gradle/libs.versions.toml` (a FILE_WRITE — approved by [SecurityPolicy]);
 * never performs git or network-upload actions.
 */
class DependencyUpdater(
    private val rootDir: File,
    private val verifyTask: String,
    private val log: (String) -> Unit,
    private val suggest: (String) -> Unit,
) {
    private val catalog = File(rootDir, "gradle/libs.versions.toml")
    private val report = File(rootDir, "reports/dependencies/dependency-updates.txt")
    private val gradlew = File(rootDir, "gradlew")

    /** version keys never auto-bumped (coordinated manual upgrades) */
    private val pinnedSubstrings = listOf("kotlin", "compose", "gradle", "intellij")

    private fun isNonStable(v: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { v.uppercase().contains(it) }
        val numeric = Regex("^[0-9,.v-]+(-r)?$")
        return !(stableKeyword || numeric.matches(v))
    }

    fun run() {
        if (!report.exists()) { suggest("Dependency update skipped: ${report.relativeTo(rootDir)} not found. Run `./gradlew :composeApp:dependencyUpdates` first."); return }
        if (!catalog.exists()) { suggest("Dependency update skipped: ${catalog.relativeTo(rootDir)} not found."); return }

        val originalToml = catalog.readText()
        val versionKeys = parseVersions(originalToml)        // key -> current value
        val moduleToKey = parseModuleKeys(originalToml)      // "group:artifact" / plugin id -> key

        val candidates = parseReport().mapNotNull { (module, _, new) ->
            val key = moduleToKey[module] ?: moduleToKey[module.removePluginSuffix()] ?: return@mapNotNull null
            if (key in versionKeys && versionKeys[key] != new && isApplicable(key, new)) Bump(key, versionKeys[key]!!, new) else null
        }.distinctBy { it.key }

        if (candidates.isEmpty()) { log("[dep-update] nothing to update (stable, non-pinned)"); return }
        log("[dep-update] candidate updates: " + candidates.joinToString { "${it.key} ${it.old}->${it.new}" })

        // 1) apply all at once, verify once
        applyAll(originalToml, candidates)
        if (verify()) {
            summarize(applied = candidates, reverted = emptyList())
            return
        }

        // 2) failed together -> revert all, then bisect per-library
        log("[dep-update] combined build failed; bisecting per library")
        val applied = mutableListOf<Bump>()
        val reverted = mutableListOf<Bump>()
        var base = originalToml
        for (b in candidates) {
            applyAll(base, listOf(b))
            if (verify()) { applied += b; base = catalog.readText() }
            else { reverted += b; catalog.writeText(base) } // keep prior good state
        }
        summarize(applied, reverted)
    }

    private data class Bump(val key: String, val old: String, val new: String)

    private fun isApplicable(key: String, new: String): Boolean {
        if (isNonStable(new)) return false
        val k = key.lowercase()
        return pinnedSubstrings.none { it in k }
    }

    private fun applyAll(fromToml: String, bumps: List<Bump>) {
        var text = fromToml
        for (b in bumps) {
            // match:  key = "old"   (tolerant of spacing/quotes)
            val rx = Regex("(^|\\n)(\\s*${Regex.escape(b.key)}\\s*=\\s*)\"[^\"]*\"")
            text = rx.replace(text) { m -> "${m.groupValues[1]}${m.groupValues[2]}\"${b.new}\"" }
        }
        catalog.writeText(text)
    }

    private fun verify(): Boolean {
        if (!gradlew.canExecute()) { log("[dep-update] gradlew not executable — cannot verify; treating as failure"); return false }
        return runCatching {
            val proc = ProcessBuilder("./gradlew", verifyTask, "--console=plain", "--no-daemon")
                .directory(rootDir)
                .redirectErrorStream(true)
                .start()
            val out = proc.inputStream.bufferedReader().readText()
            val code = proc.waitFor()
            if (code != 0) log("[dep-update] verify `$verifyTask` failed (exit $code): " + out.lineSequence().lastOrNull { it.isNotBlank() })
            code == 0
        }.getOrElse { log("[dep-update] verify error: ${it.message}"); false }
    }

    private fun summarize(applied: List<Bump>, reverted: List<Bump>) {
        val sb = StringBuilder("Dependency auto-update (non-invasive, stable, build-verified):\n")
        if (applied.isEmpty()) sb.append("  applied: none\n")
        else applied.forEach { sb.append("  + ${it.key}: ${it.old} -> ${it.new}\n") }
        if (reverted.isNotEmpty()) reverted.forEach { sb.append("  ! reverted (build failed): ${it.key} ${it.old} -> ${it.new}\n") }
        log("[dep-update] " + sb.toString().trim().replace("\n", "\n[dep-update] "))
        suggest(sb.toString())
    }

    // ---- parsing ------------------------------------------------------------------------------

    /** Report lines like: ` - io.coil-kt.coil3:coil-compose [3.2.0 -> 3.3.0]` */
    private fun parseReport(): List<Triple<String, String, String>> {
        val rx = Regex("^\\s*-\\s+(\\S+:\\S+)\\s+\\[(.+?)\\s*->\\s*(.+?)]\\s*$")
        return report.readLines().mapNotNull { line ->
            rx.find(line)?.let { Triple(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
        }
    }

    private fun parseVersions(toml: String): Map<String, String> {
        val out = linkedMapOf<String, String>()
        var inVersions = false
        val kv = Regex("^\\s*([A-Za-z0-9_.-]+)\\s*=\\s*\"([^\"]*)\"\\s*$")
        for (line in toml.lines()) {
            val tl = line.trim()
            if (tl.startsWith("[")) { inVersions = tl == "[versions]"; continue }
            if (inVersions && !tl.startsWith("#")) kv.find(line)?.let { out[it.groupValues[1]] = it.groupValues[2] }
        }
        return out
    }

    /** Map "group:artifact" (libraries) and plugin ids to their `version.ref` key. */
    private fun parseModuleKeys(toml: String): Map<String, String> {
        val out = hashMapOf<String, String>()
        val moduleRx = Regex("module\\s*=\\s*\"([^\"]+)\"")
        val idRx = Regex("\\bid\\s*=\\s*\"([^\"]+)\"")
        val refRx = Regex("version\\.ref\\s*=\\s*\"([^\"]+)\"")
        for (line in toml.lines()) {
            val ref = refRx.find(line)?.groupValues?.get(1) ?: continue
            moduleRx.find(line)?.let { out[it.groupValues[1]] = ref }
            idRx.find(line)?.let { out[it.groupValues[1]] = ref }
        }
        return out
    }

    /** Ben Manes lists plugins as `id:id.gradle.plugin`; reduce to the plugin id. */
    private fun String.removePluginSuffix(): String {
        val parts = split(":")
        return if (parts.size == 2 && parts[1].endsWith(".gradle.plugin")) parts[0] else this
    }
}
