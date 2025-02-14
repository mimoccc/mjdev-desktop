package org.mjdev.desktop.extensions

import okio.Path
import org.mjdev.desktop.system.Filesystem

@Suppress("unused", "MemberVisibilityCanBePrivate")
object PathExt {
    val cwd: Path = Filesystem.cwd()

    val Path.absolutePath: String
        get() = Filesystem.absolutePath(this)

    val Path.bytes: ByteArray
        get() =  Filesystem.readBytes(this)

    val Path.extension: String
        get() = Filesystem.extension(this)

    val Path.isDirectory: Boolean
        get() = Filesystem.isDirectory(this)

    val Path.isFile: Boolean
        get() = Filesystem.isFile(this)

    val Path.isSymbolicLink: Boolean
        get() = Filesystem.isSymbolicLink(this)

    operator fun Path.get(name:String) = resolve(name, true)

    fun Path.listFilesOnly(
        ext: String? = null,
    ) = listFilesFiltered(ext) { f -> f.isFile && !f.isDirectory }

    fun Path.listFilesFiltered(
        ext: String? = null,
        predicate: (Path) -> Boolean
    ) = listFiles(ext).filter(predicate)

    fun Path.listFiles(
        ext: String? = null
    ): List<Path> = if (exists) Filesystem.listFiles(this).let { list ->
        when {
            ext != null -> list.filter { f -> f.extension == ext }
            else -> list.toList()
        }
    } else emptyList()

    val Path.exists : Boolean
        get() = Filesystem.fileExists(this)

    val Path.parentFile : Path
        get() = this.parent ?: cwd

    fun Path.writeText(text:String) = Filesystem.writeText(this, text)

    fun Path.mkdirs() = Filesystem.createDir(this)

    fun Path.delete() = Filesystem.delete(this)

    fun Path.createNewFile() = Filesystem.createNewFile(this)

//    fun Path.listFiles(
//        ext: String? = null
//    ): List<Path> = if (exists(this)) listFiles().let { list ->
//        when {
//            list == null -> emptyList()
//            ext != null -> list.filter { f -> f.extension == ext }
//            else -> list.toList()
//        }
//    } else emptyList()

//    fun Path.listFilesFiltered(
//        ext: String? = null,
//        predicate: (Path) -> Boolean
//    ) = listFiles(ext).filter(predicate)

//    fun Path.listFilesOnly(
//        ext: String? = null,
//    ) = listFilesFiltered(ext) { f -> !f.isDirectory }

    fun List<Path>.sortedByName() =
        sortedBy { f -> f.name }

    fun List<Path>.sortedByNameDescending() =
        sortedByDescending { f -> f.name }

    inline fun <R> Path.listFiles(
        ext: String? = null,
        mapper: (Path) -> R
    ): List<R> = listFiles(ext).map(mapper)

    inline fun <R : Comparable<R>> Path.listFilesSortedBy(
        ext: String? = null,
        crossinline selector: (Path) -> R?
    ): List<Path> = listFiles(ext).sortedBy(selector)

    val Path.nameWithoutExtension
        get() = name.let {
            if (it.contains(".$extension")) it.replace(".$extension", "")
            else it
        }

    val Path.all
        get() = listFiles()

    val Path.filesOnly
        get() = listFiles().filter { !it.isDirectory }

    val Path.dirsOnly
        get() = listFiles().filter { it.isDirectory }

    val Path.lines: List<String>
        get() = runCatching {
            if (this.exists) Filesystem.readLines(this)
            else null
        }.getOrNull() ?: emptyList()

    val Path.text: String
        get() = runCatching {
            if (this.exists) Filesystem.readText(this)
            else null
        }.getOrNull().orEmpty()
}