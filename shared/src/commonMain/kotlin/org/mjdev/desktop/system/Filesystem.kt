package org.mjdev.desktop.system

import okio.Path

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object Filesystem : okio.FileSystem {
    fun fileExists(path: Path): Boolean

    fun absolutePath(path: Path): String

    fun extension(path: Path): String

    fun isDirectory(path: Path): Boolean

    fun isFile(path: Path): Boolean

    fun isSymbolicLink(path: Path): Boolean

    fun cwd(): Path

    fun readBytes(path: Path): ByteArray

    fun readText(path: Path): String

    fun readLines(path: Path): List<String>

    fun createNewFile(path: Path)

    fun createDir(path: Path)

    fun writeText(
        path: Path,
        text: String,
    )

    fun listFiles(path: Path): List<Path>

    fun deleteDir(path: Path)
}
