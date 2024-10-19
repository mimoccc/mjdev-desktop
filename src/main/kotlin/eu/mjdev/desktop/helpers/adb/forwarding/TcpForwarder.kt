package eu.mjdev.desktop.helpers.adb.forwarding

import eu.mjdev.desktop.helpers.adb.IAdb
import eu.mjdev.desktop.log.Log
import okio.*
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ServerSocket
import java.net.SocketException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

@Suppress("SameParameterValue")
internal class TcpForwarder(
    private val adb: IAdb,
    private val targetPort: Int,
    private val hostPort: Int? = null,
) : AutoCloseable {
    private var state: State = State.STOPPED
    private var serverThread: Thread? = null
    private var server: ServerSocket? = null
    private var clientExecutor: ExecutorService? = null

    fun start(): Int {
        check(state == State.STOPPED) { "Forwarder is already started at port $hostPort" }
        moveToState(State.STARTING)
        clientExecutor = Executors.newCachedThreadPool()
        serverThread = thread {
            try {
                handleForwarding()
            } catch (_: SocketException) {
                // Do nothing
            } catch (e: IOException) {
                Log.e("could not start TCP port forwarding: ${e.message}")
                Log.e(e)
            } finally {
                moveToState(State.STOPPED)
            }
        }
        waitFor(10, 5000) {
            state == State.STARTED
        }
        return server!!.localPort
    }

    private fun handleForwarding() {
        val serverRef = ServerSocket(hostPort ?: 0)
        server = serverRef
        moveToState(State.STARTED)
        while (!Thread.interrupted()) {
            val client = serverRef.accept()
            clientExecutor?.execute {
                val adbStream = adb.open("tcp:$targetPort")
                val readerThread = thread {
                    forward(
                        client.getInputStream().source(),
                        adbStream.sink
                    )
                }
                try {
                    forward(
                        adbStream.source,
                        client.sink().buffer()
                    )
                } finally {
                    adbStream.close()
                    client.close()
                    readerThread.interrupt()
                }
            }
        }
    }

    override fun close() {
        if (state == State.STOPPED || state == State.STOPPING) {
            return
        }
        waitFor(10, 5000) {
            state == State.STARTED
        }
        moveToState(State.STOPPING)
        server?.close()
        server = null
        serverThread?.interrupt()
        serverThread = null
        clientExecutor?.shutdown()
        clientExecutor?.awaitTermination(5, TimeUnit.SECONDS)
        clientExecutor = null
        waitFor(10, 5000) {
            state == State.STOPPED
        }
    }

    private fun forward(source: Source, sink: BufferedSink) {
        try {
            while (!Thread.interrupted()) {
                try {
                    if (source.read(sink.buffer, 256) >= 0) {
                        sink.flush()
                    } else {
                        return
                    }
                } catch (_: IOException) {
                    // Do nothing
                }
            }
        } catch (_: InterruptedException) {
            // Do nothing
        } catch (_: InterruptedIOException) {
            // do nothing
        }
    }

    private fun moveToState(state: State) {
        this.state = state
    }

    private enum class State {
        STARTING,
        STARTED,
        STOPPING,
        STOPPED
    }

    private fun waitFor(
        intervalMs: Int,
        timeoutMs: Int,
        test: () -> Boolean
    ) {
        val start = System.currentTimeMillis()
        var lastCheck = start
        while (!test()) {
            val now = System.currentTimeMillis()
            val timeSinceStart = now - start
            val timeSinceLastCheck = now - lastCheck
            if (timeoutMs in 0..timeSinceStart) {
                throw TimeoutException()
            }
            val sleepTime = intervalMs - timeSinceLastCheck
            if (sleepTime > 0) {
                Thread.sleep(sleepTime)
            }
            lastCheck = System.currentTimeMillis()
        }
    }
}
