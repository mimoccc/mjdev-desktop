/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.backgrounds

import eu.mjdev.desktop.extensions.Custom.get
import eu.mjdev.desktop.extensions.Custom.listFilesOnly
import eu.mjdev.desktop.helpers.internal.ImagesProvider
import eu.mjdev.desktop.provider.AppsProvider.Companion.DIR_NAME_BACKGROUNDS
import eu.mjdev.desktop.provider.AppsProvider.Companion.DIR_NAME_DOT_LOCAL
import eu.mjdev.desktop.provider.AppsProvider.Companion.DIR_NAME_SHARE
import eu.mjdev.desktop.provider.DesktopProvider

// todo
@Suppress("unused")
class ProviderLocal(
    private val api: DesktopProvider
) : ImagesProvider {
    private val localDir
        get() = api.homeDir[DIR_NAME_DOT_LOCAL]
    private val localShareDir
        get() = localDir[DIR_NAME_SHARE]
    private val backgroundFilesDir
        get() = localShareDir[DIR_NAME_BACKGROUNDS]

    override suspend fun get(): Any {
        return backgroundFilesDir.listFilesOnly()
    }
}