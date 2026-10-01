package org.mjdev.desktop.plugins.music

import okio.Path
import org.mjdev.desktop.extensions.PathExt.absolutePath
import org.mjdev.desktop.extensions.PathExt.dirsOnly
import org.mjdev.desktop.extensions.PathExt.extension
import org.mjdev.desktop.extensions.PathExt.filesOnly
import org.mjdev.desktop.extensions.PathExt.nameWithoutExtension
import org.mjdev.desktop.extensions.PathExt.sortedByName

/** Finds the playable files of a music folder. Reads only, nothing is ever changed or deleted. */
object MusicLibrary {
    /**
     * Scans [root] and its sub-folders (up to [MusicDefaults.MAX_SCAN_DEPTH] levels) and returns
     * the playable tracks sorted by path, at most [MusicDefaults.MAX_TRACKS] of them.
     */
    fun scan(root: Path): List<MusicTrack> = runCatching { collect(root, 0) }
        .getOrDefault(emptyList())
        .take(MusicDefaults.MAX_TRACKS)

    private fun collect(
        dir: Path,
        depth: Int,
    ): List<MusicTrack> {
        val files =
            dir.filesOnly
                .filter { file -> file.extension.lowercase() in MusicDefaults.EXTENSIONS }
                .sortedByName()
                .map { file -> file.toTrack() }
        if (depth >= MusicDefaults.MAX_SCAN_DEPTH) return files
        val nested = dir.dirsOnly.sortedByName().flatMap { sub -> collect(sub, depth + 1) }
        return files + nested
    }

    private fun Path.toTrack() = MusicTrack(
        id = absolutePath,
        title = nameWithoutExtension,
        path = absolutePath,
    )
}
