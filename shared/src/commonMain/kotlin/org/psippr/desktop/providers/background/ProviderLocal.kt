/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.psippr.desktop.providers.background

import org.psippr.desktop.extensions.PathExt.listFilesOnly
import org.psippr.desktop.helpers.compose.ImagesProvider
import org.psippr.desktop.interfaces.IUser
import org.psippr.desktop.log.Log

// todo more folders
@Suppress("unused")
class ProviderLocal(
    private val user: IUser,
) : ImagesProvider {
    private val files
        get() =
            user.userDirs.backgroundsDirectory
                .apply {
                    Log.d("backgrounds directory: $this")
                }.listFilesOnly()
                .apply {
                    Log.d("backgrounds directory size: ${this.size}")
                }

    override val size: Int
        get() = files.size

    override suspend fun get(): Any = files
}
