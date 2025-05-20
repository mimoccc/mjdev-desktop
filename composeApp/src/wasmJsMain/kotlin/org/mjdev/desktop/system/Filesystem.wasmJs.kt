package org.mjdev.desktop.system

import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object Filesystem : FileSystem() {
    actual fun absolutePath(path: Path): String {
        TODO("Not yet implemented")
    }

    actual fun extension(
        path: Path
    ): String = path.name.split(".").let {
        if (it.size>0) it.last() else ""
    }

    actual fun isDirectory(
        path: Path
    ): Boolean = metadataOrNull(path)?.isDirectory == true

    actual fun isFile(
        path: Path
    ): Boolean = metadataOrNull(path)?.isRegularFile == true

    actual fun isSymbolicLink(
        path: Path
    ): Boolean = metadata(path)?.symlinkTarget != null

    actual fun cwd(): Path {
        TODO("Not yet implemented")
    }

    actual fun readBytes(path: Path): ByteArray {
        TODO("Not yet implemented")
    }

    actual fun readText(path: Path): String {
        TODO("Not yet implemented")
    }

    actual fun readLines(path: Path): List<String> {
        TODO("Not yet implemented")
    }

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun atomicMove(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun canonicalize(path: Path): Path {
        TODO("Not yet implemented")
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        TODO("Not yet implemented")
    }

    override fun createSymlink(source: Path, target: Path) {
        TODO("Not yet implemented")
    }

    override fun delete(path: Path, mustExist: Boolean) {
        TODO("Not yet implemented")
    }

    override fun list(dir: Path): List<Path> {
        TODO("Not yet implemented")
    }

    override fun listOrNull(dir: Path): List<Path>? {
        TODO("Not yet implemented")
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        TODO("Not yet implemented")
    }

    override fun openReadOnly(file: Path): FileHandle {
        TODO("Not yet implemented")
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        TODO("Not yet implemented")
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun source(file: Path): Source {
        TODO("Not yet implemented")
    }

    actual fun writeText(path: Path, text: String) {
        TODO("Not yet implemented")
    }

    actual fun createNewFile(path: Path) {
    }

    actual fun fileExists(path: Path): Boolean {
        TODO("Not yet implemented")
    }

    actual fun createDir(path: Path) {
    }

    actual fun listFiles(path: Path): List<Path> {
        TODO("Not yet implemented")
    }
}