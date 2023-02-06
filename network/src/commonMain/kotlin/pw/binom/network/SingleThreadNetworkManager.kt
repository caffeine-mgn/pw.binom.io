package pw.binom.network

import kotlinx.coroutines.*
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicLong
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.io.socket.KeyListenFlags
import pw.binom.io.socket.SelectedKeys
import pw.binom.io.socket.Selector
import pw.binom.processing
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

class SingleThreadNetworkManager : AbstractNetworkManager(), Closeable {

    private var closed = AtomicBoolean(false)
    override val selector = Selector()

    private val exchange = BatchExchange<Runnable>()

    private fun ensureStarted() {
        ensureOpen()
        if (currentThreadId.getValue() != 0L) {
            throw IllegalStateException("NetworkDispatcher not started")
        }
    }

    override fun ensureOpen() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        ensureOpen()
        this.exchange.push(block)
        wakeup()
    }

    private var currentThreadId = AtomicLong(0)

    private fun executeLazyTasks() {
        exchange.popAll {
            if (it.isEmpty()) {
                return@popAll
            }
            it.forEach {
                try {
                    it.run()
                } catch (e: Throwable) {
                    val currentThread = Thread.currentThread
                    currentThread.uncaughtExceptionHandler.uncaughtException(
                        thread = currentThread,
                        throwable = RuntimeException("Error on network queue", e)
                    )
                }
            }
        }
    }

    private val selectedKeys = SelectedKeys()

    @Suppress("OPT_IN_IS_NOT_ENABLED")
    @OptIn(DelicateCoroutinesApi::class)
    fun start(func: suspend CoroutineScope.() -> Unit) {
        val b = GlobalScope.launch(start = CoroutineStart.LAZY) {
            try {
                withContext(this@SingleThreadNetworkManager) {
                    func(this)
                }
                close()
            } catch (e: Throwable) {
                e.processing {
                    close()
                }
            }
        }
        this.exchange.push(
            Runnable {
                b.start()
            }
        )
        start()
    }

    fun start() {
        ensureOpen()
        if (!currentThreadId.compareAndSet(0, Thread.currentThread.id)) {
            throw IllegalStateException("NetworkManager already started")
        }

        try {
            while (!closed.getValue()) {
                executeLazyTasks()
                var v = 0
                this.selector.select(timeout = Duration.INFINITE, selectedKeys = selectedKeys)
                selectedKeys.forEach { event ->
                    v++
                    try {
                        val attachment = event.key.attachment
                        attachment ?: error("Attachment is null")
                        val connection = attachment as AbstractConnection
                        when {
                            event.key.readFlags and KeyListenFlags.ERROR != 0 -> connection.error()
                            event.key.readFlags and KeyListenFlags.WRITE != 0 -> connection.readyForWrite(event.key)
                            event.key.readFlags and KeyListenFlags.READ != 0 -> connection.readyForRead(event.key)
                            else -> error("Unknown connection event")
                        }
                    } catch (e: Throwable) {
                        val currentThread = Thread.currentThread
                        currentThread.uncaughtExceptionHandler.uncaughtException(
                            thread = currentThread,
                            throwable = e,
                        )
                    }
                }
                executeLazyTasks()
            }
        } catch (e: Throwable) {
            val currentThread = Thread.currentThread
            currentThread.uncaughtExceptionHandler.uncaughtException(currentThread, e)
        } finally {
            closed.setValue(true)
        }
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean =
        Thread.currentThread.id != currentThreadId.getValue()

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            wakeup()
        }
    }
}
