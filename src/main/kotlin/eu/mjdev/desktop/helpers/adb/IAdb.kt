package eu.mjdev.desktop.helpers.adb

import eu.mjdev.desktop.helpers.adb.adbserver.AdbServer
import eu.mjdev.desktop.helpers.adb.forwarding.TcpForwardDescriptor
import eu.mjdev.desktop.helpers.adb.forwarding.TcpForwarder
import eu.mjdev.desktop.helpers.adb.helpers.*
import okio.*
import java.io.File
import java.nio.file.Files

@Suppress("unused")
interface IAdb : AutoCloseable {

    val host: String
    val port: Int
    val deviceQuery: String
    val name: String

    @Throws(IOException::class)
    fun open(destination: String): IAdbStream

    fun supportsFeature(feature: String): Boolean

    @Throws(IOException::class)
    fun shell(command: String): AdbShellResponse {
        openShell(command).use { stream ->
            return stream.readAll()
        }
    }

    @Throws(IOException::class)
    fun openShell(command: String = ""): AdbShellStream {
        val stream = open("shell,v2,raw:$command")
        return AdbShellStream(stream)
    }

    @Throws(IOException::class)
    fun push(src: File, remotePath: String, mode: Int = readMode(src), lastModifiedMs: Long = src.lastModified()) {
        push(src.source(), remotePath, mode, lastModifiedMs)
    }

    @Throws(IOException::class)
    fun push(source: Source, remotePath: String, mode: Int, lastModifiedMs: Long) {
        openSync().use { stream ->
            stream.send(source, remotePath, mode, lastModifiedMs)
        }
    }

    @Throws(IOException::class)
    fun pull(dst: File, remotePath: String) {
        pull(dst.sink(append = false), remotePath)
    }

    @Throws(IOException::class)
    fun pull(sink: Sink, remotePath: String) {
        openSync().use { stream ->
            stream.recv(sink, remotePath)
        }
    }

    @Throws(IOException::class)
    fun openSync(): AdbSyncStream {
        val stream = open("sync:")
        return AdbSyncStream(stream)
    }

    @Throws(IOException::class)
    fun install(file: File, vararg options: String) {
        if (supportsFeature("cmd")) {
            install(file.source(), file.length(), *options)
        } else {
            pmInstall(file, *options)
        }
    }

    @Throws(IOException::class)
    fun install(source: Source, size: Long, vararg options: String) {
        if (supportsFeature("cmd")) {
            execCmd("package", "install", "-S", size.toString(), *options).use { stream ->
                stream.sink.writeAll(source)
                stream.sink.flush()
                val response = stream.source.readString(Charsets.UTF_8)
                if (!response.startsWith("Success")) {
                    throw IOException("Install failed: $response")
                }
            }
        } else {
            val tempFile = kotlin.io.path.createTempFile()
            val fileSink = tempFile.sink().buffer()
            fileSink.writeAll(source)
            fileSink.flush()
            pmInstall(tempFile.toFile(), *options)
        }
    }

    private fun pmInstall(file: File, vararg options: String) {
        val fileName = file.name
        val remotePath = "/data/local/tmp/$fileName"
        push(file, remotePath)
        shell("pm install ${options.joinToString(" ")} \"$remotePath\"")
    }

    @Throws(IOException::class)
    fun installMultiple(apks: List<File>, vararg options: String) {
        if (supportsFeature("cmd")) {
            val totalLength = apks.map { it.length() }.reduce { acc, l -> acc + l }
            execCmd("package", "install-create", "-S", totalLength.toString(), *options).use { createStream ->
                val response = createStream.source.readString(Charsets.UTF_8)
                if (!response.startsWith("Success")) {
                    throw IOException("connect error for create: $response")
                }
                val pattern = """\[(\w+)]""".toRegex()
                val sessionId =
                    pattern.find(response)?.groups?.get(1)?.value ?: throw IOException("failed to create session")
                var error: String? = null
                apks.forEach { apk ->
                    execCmd(
                        "package",
                        "install-write",
                        "-S",
                        apk.length().toString(),
                        sessionId,
                        apk.name,
                        "-",
                        *options
                    ).use { writeStream ->
                        writeStream.sink.writeAll(apk.source())
                        writeStream.sink.flush()
                        val writeResponse = writeStream.source.readString(Charsets.UTF_8)
                        if (!writeResponse.startsWith("Success")) {
                            error = writeResponse
                            return@forEach
                        }
                    }
                }
                val finalCommand = if (error == null) "install-commit" else "install-abandon"
                execCmd("package", finalCommand, sessionId, *options).use { commitStream ->
                    val finalResponse = commitStream.source.readString(Charsets.UTF_8)
                    if (!finalResponse.startsWith("Success")) {
                        throw IOException("failed to finalize session: $commitStream")
                    }
                }
                if (error != null) {
                    throw IOException("Install failed: $error")
                }
            }
        } else {
            val totalLength = apks.map { it.length() }.reduce { acc, l -> acc + l }
            val response = shell("pm install-create -S $totalLength ${options.joinToString(" ")}")
            if (!response.allOutput.startsWith("Success")) {
                throw IOException("pm create session failed: $response")
            }
            val pattern = """\[(\w+)]""".toRegex()
            val sessionId =
                pattern.find(response.allOutput)?.groups?.get(1)?.value ?: throw IOException("failed to create session")
            var error: String? = null
            val fileNames = apks.map { it.name }
            val remotePaths = fileNames.map { "/data/local/tmp/$it" }
            apks.zip(remotePaths).forEachIndexed { index, pair ->
                val apk = pair.first
                val remotePath = pair.second
                try {
                    push(apk, remotePath)
                } catch (t: IOException) {
                    error = t.message
                    return@forEachIndexed
                }
                val writeResponse = shell("pm install-write -S ${apk.length()} $sessionId $index $remotePath")
                if (!writeResponse.allOutput.startsWith("Success")) {
                    error = writeResponse.allOutput
                    return@forEachIndexed
                }
            }
            val finalCommand = if (error == null) "pm install-commit $sessionId" else "pm install-abandon $sessionId"
            val finalResponse = shell(finalCommand)
            if (!finalResponse.allOutput.startsWith("Success")) {
                throw IOException("failed to finalize session: $finalResponse")
            }
            if (error != null) {
                throw IOException("Install failed: $error")
            }
        }
    }

    @Throws(IOException::class)
    fun uninstall(packageName: String) {
        val response = shell("cmd package uninstall $packageName")
        if (response.exitCode != 0) {
            throw IOException("Uninstall failed: ${response.allOutput}")
        }
    }

    @Throws(IOException::class)
    fun execCmd(vararg command: String): IAdbStream {
        if (!supportsFeature("cmd")) throw UnsupportedOperationException("cmd is not supported on this version of Android")
        val destination = (listOf("exec:cmd") + command).joinToString(" ")
        return open(destination)
    }

    @Throws(IOException::class)
    fun abbExec(vararg command: String): IAdbStream {
        if (!supportsFeature("abb_exec")) throw UnsupportedOperationException("abb_exec is not supported on this version of Android")
        val destination = "abb_exec:${command.joinToString("\u0000")}"
        return open(destination)
    }

    @Throws(IOException::class)
    fun root() {
        val response = restartAdb(this, "root:")
        if (!response.startsWith("restarting") && !response.contains("already")) {
            throw IOException("Failed to restart adb as root: $response")
        }
        waitRootOrClose(this, root = true)
    }

    @Throws(IOException::class)
    fun unRoot() {
        val response = restartAdb(this, "unRoot:")
        if (!response.startsWith("restarting") && !response.contains("not running as root")) {
            throw IOException("Failed to restart adb as root: $response")
        }
        waitRootOrClose(this, root = false)
    }

    @Throws(InterruptedException::class)
    fun tcpForward(targetPort: Int, hostPort: Int): TcpForwardDescriptor {
        val forwarder = TcpForwarder(this, targetPort, hostPort)
        val localPort = forwarder.start()
        return TcpForwardDescriptor(forwarder, localPort)
    }

    @Throws(InterruptedException::class)
    fun tcpForward(targetPort: Int): TcpForwardDescriptor {
        val forwarder = TcpForwarder(this, targetPort)
        val localPort = forwarder.start()
        return TcpForwardDescriptor(forwarder, localPort)
    }

    companion object {
        private const val MIN_EMULATOR_PORT = 5555
        private const val MAX_EMULATOR_PORT = 5683

        @JvmStatic
        @JvmOverloads
        fun create(
            host: String,
            port: Int,
            keyPair: AdbKeyPair? = AdbKeyPair.readDefault(),
            connectTimeout: Int = 0,
            socketTimeout: Int = 0
        ): IAdb = AdbImpl(host, port, keyPair, connectTimeout, socketTimeout)

        @JvmStatic
        @JvmOverloads
        fun discover(
            host: String = "localhost",
            keyPair: AdbKeyPair? = AdbKeyPair.readDefault()
        ): List<IAdb> {
            return list(host, keyPair)
        }

        @JvmStatic
        @JvmOverloads
        fun list(host: String = "localhost", keyPair: AdbKeyPair? = AdbKeyPair.readDefault()): List<IAdb> {
            val dadbs = AdbServer.listAdbs(adbServerHost = host)
            if (dadbs.isNotEmpty()) return dadbs
            return (MIN_EMULATOR_PORT..MAX_EMULATOR_PORT).mapNotNull { port ->
                val dadb = create(host, port, keyPair)
                val response = try {
                    dadb.shell("echo success").allOutput
                } catch (ignore: Throwable) {
                    null
                }
                if (response == "success\n") {
                    dadb
                } else {
                    null
                }
            }
        }

        private fun waitRootOrClose(dadb: IAdb, root: Boolean) {
            while (true) {
                try {
                    val response = dadb.shell("getprop service.adb.root")
                    val propValue = if (root) 1 else 0
                    if (response.output == "$propValue\n") return
                } catch (e: IOException) {
                    return
                }
            }
        }

        private fun restartAdb(dadb: IAdb, destination: String): String {
            dadb.open(destination).use { stream ->
                return stream.source.readUntil('\n'.code.toByte()).readString(Charsets.UTF_8)
            }
        }

        private fun BufferedSource.readUntil(endByte: Byte): Buffer {
            val buffer = Buffer()
            while (true) {
                val b = readByte()
                buffer.writeByte(b.toInt())
                if (b == endByte) return buffer
            }
        }

        private fun readMode(file: File): Int {
            return Files.getAttribute(file.toPath(), "unix:mode") as? Int
                ?: throw RuntimeException("Unable to read file mode")
        }
    }
}
