package org.mjdev.desktop.system

import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.buffer
import java.io.File
import java.nio.charset.Charset

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object Filesystem : FileSystem() {
    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        return SYSTEM.appendingSink(file, mustExist)
    }

    override fun atomicMove(source: Path, target: Path) {
        SYSTEM.atomicMove(source, target)
    }

    override fun canonicalize(path: Path): Path {
        return SYSTEM.canonicalize(path)
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        return SYSTEM.createDirectory(dir, mustCreate)
    }

    override fun createSymlink(source: Path, target: Path) {
        return SYSTEM.createSymlink(source, target)
    }

    override fun delete(path: Path, mustExist: Boolean) {
        return SYSTEM.delete(path, mustExist)
    }

    override fun list(dir: Path): List<Path> {
        return SYSTEM.list(dir)
    }

    override fun listOrNull(dir: Path): List<Path>? {
        return SYSTEM.listOrNull(dir)
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        return SYSTEM.metadataOrNull(path)
    }

    override fun openReadOnly(file: Path): FileHandle {
        return SYSTEM.openReadOnly(file)
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        return SYSTEM.openReadWrite(file, mustCreate, mustExist)
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        return SYSTEM.sink(file, mustCreate)
    }

    override fun source(file: Path): Source {
        return SYSTEM.source(file)
    }

    actual fun absolutePath(path: Path): String {
        return path.toFile().absolutePath
    }

    actual fun extension(path: Path): String {
        return path.toFile().extension
    }

    actual fun isDirectory(path: Path): Boolean {
        return path.toFile().isDirectory
    }

    actual fun isFile(path: Path): Boolean {
        return path.toFile().isFile
    }

    actual fun isSymbolicLink(path: Path): Boolean {
        return false // todo
    }

    actual fun cwd(): Path {
        return File(".").absolutePath.toPath(normalize = true)
    }

    actual fun readBytes(path: Path): ByteArray {
        return SYSTEM.source(path).buffer().readByteArray()
    }

    actual fun readText(path: Path): String {
        return SYSTEM.source(path).buffer().readString(Charset.forName("UTF-8"))
    }

    actual fun readLines(path: Path): List<String> {
        return readText(path)
            .replace("\r", "")
            .split("\n")
    }

    actual fun writeText(path: Path, text: String) {
        SYSTEM.sink(path).buffer().write(text.encodeToByteArray()).flush()
    }

    actual fun createNewFile(path: Path) {
        path.toFile().createNewFile()
    }

    actual fun fileExists(path: Path): Boolean {
        return SYSTEM.exists(path)
    }

    actual fun createDir(path: Path) {
        SYSTEM.createDirectory(path)
    }

    actual fun listFiles(path: Path): List<Path> {
        return listOrNull(path) ?: emptyList()
    }

    actual fun deleteDir(path: Path) {
        SYSTEM.deleteRecursively(path)
    }
}