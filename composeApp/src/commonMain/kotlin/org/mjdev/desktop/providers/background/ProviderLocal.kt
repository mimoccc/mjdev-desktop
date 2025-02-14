/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.providers.background

import org.mjdev.desktop.helpers.compose.ImagesProvider
import org.mjdev.desktop.interfaces.IUser
import org.mjdev.desktop.extensions.PathExt.listFilesOnly
import org.mjdev.desktop.log.Log

// todo more folders
@Suppress("unused")
class ProviderLocal(
    private val user: IUser
) : ImagesProvider {
    private val files
        get() = user.userDirs.backgroundsDirectory.apply{
            Log.d("backgrounds directory: $this")
        }.listFilesOnly().apply{
            Log.d("backgrounds directory size: ${this.size}")
        }

    override val size: Int
        get() = files.size

    override suspend fun get(): Any {
        return files
    }
}