/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.application

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.configureSwingGlobalsForCompose
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLocalization
import eu.mjdev.desktop.helpers.application.base.GlobalDensity
import eu.mjdev.desktop.helpers.application.base.GlobalLayoutDirection
import eu.mjdev.desktop.helpers.application.base.GlobalSnapshotManager
import eu.mjdev.desktop.helpers.application.base.YieldFrameClock
import eu.mjdev.desktop.helpers.application.l10n.defaultPlatformLocalization
import eu.mjdev.desktop.provider.DesktopProvider.Companion.LocalDesktop
import eu.mjdev.desktop.provider.DesktopProvider.Companion.rememberDesktopProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.MainUIDispatcher
import kotlin.system.exitProcess

@OptIn(ExperimentalComposeUiApi::class)
fun application(
    args: List<String> = emptyList(),
    exitProcessOnExit: Boolean = true,
    content: @Composable ApplicationScope.() -> Unit
) {
    if (System.getProperty("compose.application.configure.swing.globals") == "true") {
        configureSwingGlobalsForCompose()
    }
    runBlocking {
        awaitApplication(args) {
            content()
        }
    }
    if (exitProcessOnExit) {
        println("Exiting application.")
        exitProcess(0)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
suspend fun awaitApplication(
    args: List<String> = emptyList(),
    content: @Composable ApplicationScope.() -> Unit
) {
    val applicationScope = object : ApplicationScope(args) {
        override fun exitApplication() {
            isOpen = false
        }
    }
    if (System.getProperty("compose.application.configure.swing.globals") == "true") {
        configureSwingGlobalsForCompose()
    }
    withContext(MainUIDispatcher) {
        withContext(YieldFrameClock) {
            GlobalSnapshotManager.ensureStarted()
            val recomposer = Recomposer(coroutineContext)
            launch {
                recomposer.runRecomposeAndApplyChanges()
            }
            launch {
                val applier = ApplicationApplier()
                val composition = Composition(applier, recomposer)
                try {
                    composition.setContent {
                        if (applicationScope.isOpen) {
                            CompositionLocalProvider(
                                LocalDensity provides GlobalDensity,
                                LocalLayoutDirection provides GlobalLayoutDirection,
                                LocalLocalization providesDefault defaultPlatformLocalization(),
                                LocalDesktop provides rememberDesktopProvider(
                                    application = applicationScope,
                                    args = args
                                )
                            ) {
                                applicationScope.content()
                            }
                        }
                    }
                    recomposer.close()
                    recomposer.join()
                } finally {
                    composition.dispose()
                }
            }
        }
    }
}
