package ai

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * A pluggable AI backend used by [RunAiAgentTask].
 *
 * Implementations: [OllamaProvider] (local), [MistralProvider] (cloud),
 * [ClaudeCodeProvider] (claude-code CLI). Selection is handled by [AiProviders.select].
 */
interface AiProvider {
    /** Human readable provider id, e.g. "claude-code". */
    val name: String

    /** True when the backend can actually be reached / invoked right now. */
    fun isAvailable(): Boolean

    /** Whether this provider talks to a remote cloud service (vs a local process). */
    val isCloud: Boolean

    /** Send [prompt] and return the model's text answer. Throws on transport errors. */
    fun complete(prompt: String): String
}

/** Tiny stdlib-only JSON helpers — buildSrc has no JSON dependency on the classpath. */
object Json {
    fun escape(s: String): String = buildString {
        for (c in s) when (c) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
        }
    }

    /** Best-effort: first string value for [key] anywhere in [json] (handles \" and \\ escapes). */
    fun firstString(json: String, key: String): String? {
        val marker = "\"$key\""
        var at = json.indexOf(marker)
        while (at >= 0) {
            var j = at + marker.length
            while (j < json.length && json[j] != ':') j++
            j++
            while (j < json.length && json[j].isWhitespace()) j++
            if (j < json.length && json[j] == '"') {
                val sb = StringBuilder()
                j++
                while (j < json.length) {
                    val c = json[j]
                    when {
                        c == '\\' && j + 1 < json.length -> {
                            when (val n = json[j + 1]) {
                                'n' -> sb.append('\n'); 'r' -> sb.append('\r'); 't' -> sb.append('\t')
                                '"' -> sb.append('"'); '\\' -> sb.append('\\'); '/' -> sb.append('/')
                                'u' -> {
                                    if (j + 5 < json.length) {
                                        sb.append(json.substring(j + 2, j + 6).toInt(16).toChar()); j += 4
                                    }
                                }
                                else -> sb.append(n)
                            }
                            j += 2
                        }
                        c == '"' -> return sb.toString()
                        else -> { sb.append(c); j++ }
                    }
                }
            }
            at = json.indexOf(marker, at + marker.length)
        }
        return null
    }
}

private val http: HttpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build()

private fun post(url: String, body: String, headers: Map<String, String>): HttpResponse<String> {
    val builder = HttpRequest.newBuilder(URI.create(url))
        .timeout(Duration.ofMinutes(5))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
    headers.forEach { (k, v) -> builder.header(k, v) }
    return http.send(builder.build(), HttpResponse.BodyHandlers.ofString())
}

/** Local Ollama server (http://localhost:11434 by default). */
class OllamaProvider(
    private val host: String = System.getenv("OLLAMA_HOST") ?: "http://localhost:11434",
    private val model: String = System.getenv("OLLAMA_MODEL") ?: "llama3.1",
) : AiProvider {
    override val name = "ollama"
    override val isCloud = false

    override fun isAvailable(): Boolean = runCatching {
        val resp = http.send(
            HttpRequest.newBuilder(URI.create("$host/api/tags")).timeout(Duration.ofSeconds(3)).GET().build(),
            HttpResponse.BodyHandlers.discarding(),
        )
        resp.statusCode() in 200..299
    }.getOrDefault(false)

    override fun complete(prompt: String): String {
        val body = """{"model":"${Json.escape(model)}","prompt":"${Json.escape(prompt)}","stream":false}"""
        val resp = post("$host/api/generate", body, emptyMap())
        check(resp.statusCode() in 200..299) { "ollama HTTP ${resp.statusCode()}: ${resp.body().take(300)}" }
        return Json.firstString(resp.body(), "response") ?: resp.body()
    }

    /** Best-effort `ollama serve` launch when the daemon is down. Returns true if it became reachable. */
    fun ensureServing(log: (String) -> Unit): Boolean {
        if (isAvailable()) return true
        if (which("ollama") == null) { log("ollama binary not found on PATH"); return false }
        log("starting `ollama serve` ...")
        runCatching {
            ProcessBuilder("ollama", "serve").redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD).start()
        }.onFailure { log("failed to start ollama: ${it.message}"); return false }
        repeat(20) { if (isAvailable()) return true; Thread.sleep(500) }
        return isAvailable()
    }
}

/** Mistral cloud chat completions. */
class MistralProvider(
    private val apiKey: String,
    private val model: String = System.getenv("MISTRAL_MODEL") ?: "mistral-large-latest",
) : AiProvider {
    override val name = "mistral"
    override val isCloud = true
    override fun isAvailable() = apiKey.isNotBlank()

    override fun complete(prompt: String): String {
        val body = """{"model":"${Json.escape(model)}","messages":[{"role":"user","content":"${Json.escape(prompt)}"}]}"""
        val resp = post(
            "https://api.mistral.ai/v1/chat/completions", body,
            mapOf("Authorization" to "Bearer $apiKey"),
        )
        check(resp.statusCode() in 200..299) { "mistral HTTP ${resp.statusCode()}: ${resp.body().take(300)}" }
        return Json.firstString(resp.body(), "content") ?: resp.body()
    }
}

/**
 * The claude-code CLI in headless print mode: `claude -p "<prompt>"`.
 * Authenticates via the user's existing claude-code login — no API key in this repo.
 */
class ClaudeCodeProvider(
    private val command: List<String> = (System.getenv("CLAUDE_CODE_CMD")?.split(" ") ?: listOf("claude", "-p")),
) : AiProvider {
    override val name = "claude-code"
    override val isCloud = true
    override fun isAvailable() = which(command.first()) != null

    override fun complete(prompt: String): String {
        val proc = ProcessBuilder(command + prompt)
            .redirectErrorStream(true)
            .start()
        val out = proc.inputStream.bufferedReader().readText()
        proc.waitFor()
        return out.trim()
    }
}

/** Locate an executable on PATH (returns null when absent). */
fun which(bin: String): String? {
    val path = System.getenv("PATH") ?: return null
    return path.split(java.io.File.pathSeparator).map { java.io.File(it, bin) }
        .firstOrNull { it.canExecute() }?.absolutePath
}

object AiProviders {
    /**
     * Pick a provider. Honors an explicit `provider=` credential, otherwise
     * prefers cloud (claude-code, then mistral) and falls back to local ollama
     * (starting `ollama serve` on demand), per ai-todo.txt.
     */
    fun select(creds: Map<String, String>, log: (String) -> Unit): AiProvider? {
        val forced = creds["provider"]?.lowercase()
        val claude = ClaudeCodeProvider()
        val mistral = creds["MISTRAL_API_KEY"]?.let { MistralProvider(it) }
        val ollama = OllamaProvider(
            host = creds["OLLAMA_HOST"] ?: System.getenv("OLLAMA_HOST") ?: "http://localhost:11434",
            model = creds["OLLAMA_MODEL"] ?: System.getenv("OLLAMA_MODEL") ?: "llama3.1",
        )
        when (forced) {
            "claude-code", "claude" -> if (claude.isAvailable()) return claude
            "mistral" -> if (mistral?.isAvailable() == true) return mistral
            "ollama" -> { if (ollama.ensureServing(log)) return ollama }
        }
        // prefer cloud
        if (claude.isAvailable()) return claude
        if (mistral?.isAvailable() == true) return mistral
        // local fallback — start the daemon if needed
        if (ollama.ensureServing(log)) return ollama
        return null
    }
}
