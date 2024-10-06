/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.helpers.application.base

import java.util.concurrent.atomic.AtomicLong
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.jetbrains.skiko.MainUIDispatcher

val GlobalSnapshotManagerDispatcher: CoroutineDispatcher = MainUIDispatcher

object GlobalSnapshotManager {
    private val started = AtomicLong(0)
    private val sent = AtomicLong(0)

    fun ensureStarted() {
        if (started.compareAndSet(0, 1)) {
            val channel = Channel<Unit>(1)
            CoroutineScope(GlobalSnapshotManagerDispatcher).launch {
                channel.consumeEach {
                    sent.compareAndSet(1, 0)
                    Snapshot.sendApplyNotifications()
                }
            }
            Snapshot.registerGlobalWriteObserver {
                if (sent.compareAndSet(0, 1)) {
                    channel.trySend(Unit)
                }
            }
        }
    }
}
