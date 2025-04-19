/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package org.mjdev.desktop.helpers.application

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.configureSwingGlobalsForCompose
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import coil3.compose.LocalPlatformContext
import org.mjdev.desktop.helpers.application.base.GlobalDensity
import org.mjdev.desktop.helpers.application.base.GlobalLayoutDirection
import org.mjdev.desktop.helpers.application.base.GlobalSnapshotManager
import org.mjdev.desktop.helpers.application.base.YieldFrameClock
import org.mjdev.desktop.log.Log
import org.mjdev.desktop.context.DesktopContext.Companion.rememberDesktopContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.MainUIDispatcher
import org.mjdev.desktop.context.LocalDesktopContext
import org.mjdev.desktop.helpers.image.ImageLoader.asyncImageLoader
import org.mjdev.desktop.helpers.l10n.LocalLocalization
import org.mjdev.desktop.helpers.l10n.defaultPlatformLocalization
import java.awt.event.WindowEvent
import kotlin.system.exitProcess

@OptIn(ExperimentalComposeUiApi::class)
fun application(
    args: List<String> = emptyList(),
    exitProcessOnExit: Boolean = true,
    onExit: () -> Unit = {},
    content: @Composable ApplicationScope.() -> Unit
) {
    if (System.getProperty("compose.application.configure.swing.globals") == "true") {
        configureSwingGlobalsForCompose()
    }
    runBlocking {
        awaitApplication(
            args,
            content
        )
    }
    if (exitProcessOnExit) {
        Log.i("Exiting application.")
        onExit()
        exitProcess(0)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
suspend fun awaitApplication(
    args: List<String> = emptyList(),
    content: @Composable ApplicationScope.() -> Unit
) {
    val applicationScope = ApplicationScope(args, { code ->
        isOpen = false
        exitProcess(code)
    })
    val errorHandler = WindowExceptionHandlerFactory { window ->
        WindowExceptionHandler {
            applicationScope.exitApplication()
            window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
            throw it
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
                                LocalDesktopContext provides rememberDesktopContext(
                                    application = applicationScope,
                                    imageLoader = asyncImageLoader(),
                                    platformContext = LocalPlatformContext.current
                                ),
                                LocalWindowExceptionHandlerFactory provides errorHandler
                            ) {
                                val context = LocalDesktopContext.current
                                applicationScope.onExitProcess = {
                                    if (isOpen) {
                                        context.dispose()
                                    }
                                }
                                content(applicationScope)
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
