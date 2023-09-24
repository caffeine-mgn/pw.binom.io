package pw.binom.network

import kotlinx.coroutines.Runnable
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.io.socket.KeyListenFlags
import pw.binom.io.socket.SelectedKeys
import pw.binom.io.socket.Selector
import pw.binom.thread.Thread
import kotlin.time.Duration

open class NetworkThread : Thread, Closeable {
    private val selector: Selector
    private val exchange: BatchExchange<Runnable>

    constructor(name: String, selector: Selector, exchange: BatchExchange<Runnable>) : super(name = name) {
        this.selector = selector
        this.exchange = exchange
    }

    constructor(selector: Selector, exchange: BatchExchange<Runnable>) : super() {
        this.selector = selector
        this.exchange = exchange
    }

    //    private val selector = Selector.open()
    private val selectedKeys = SelectedKeys()
    private val closed = AtomicBoolean(false)
    private fun ensureOpen() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

//    fun wakeup() {
//        checkClosed()
//        selector.wakeup()
//    }

//    fun getAttachedKeys(): Collection<Selector.Key> {
//        checkClosed()
//        return selector.getAttachedKeys()
//    }

//    val taskSize
//        get() = readyForWriteListener.size
//
//    fun executeOnThread(block: Runnable) {
//        checkClosed()
//        readyForWriteListener.push(block)
//    }

//    private val loopWatcher by lazy { LoopWatcher("NetworkThread-$name") }

    private fun executeLazyTasks() {
        exchange.popAll {
            if (it.isEmpty()) {
                return@popAll
            }
            it.forEach {
                try {
                    it.run()
                } catch (e: Throwable) {
                    uncaughtExceptionHandler.uncaughtException(
                        thread = this,
                        throwable = RuntimeException("Error on network queue", e)
                    )
                }
            }
        }
    }

    override fun execute() {
        try {
            while (!closed.getValue()) {
//                loopWatcher.call()
//                var taskForRun = defaultMutableList<Runnable>()
                executeLazyTasks()
                var v = 0
                this.selector.select(timeout = Duration.INFINITE, selectedKeys = selectedKeys)
                selectedKeys.forEach { event ->
                    v++
                    try {
                        val attachment = event.key.attachment
                        attachment ?: error("NetworkThread::$id-$name Attachment is null")
                        val connection = attachment as AbstractConnection
                        when {
//                            event.flags and SelectorOld.EVENT_CONNECTED != 0 -> connection.connected()
                            event.key.readFlags.isError -> connection.error()
                            event.key.readFlags.isWrite -> connection.readyForWrite(event.key)
                            event.key.readFlags.isRead -> connection.readyForRead(event.key)
                            else -> error("NetworkThread::$id-$name Unknown connection event")
                        }
                    } catch (e: Throwable) {
                        uncaughtExceptionHandler.uncaughtException(
                            thread = this,
                            throwable = e,
                        )
                    }
                }
//                exchange.popAll { runnable ->
//                    runnable.forEach { runnable ->
//                        try {
//                            runnable.run()
//                        } catch (e: Throwable) {
//                            uncaughtExceptionHandler.uncaughtException(
//                                thread = this,
//                                throwable = RuntimeException("Error on network queue", e)
//                            )
//                        }
//                    }
//                }
                executeLazyTasks()
//                println("NetworkThread::$id-$name got runnable=$runnable")
//                if (runnable != null) {
//                    try {
//                        runnable.run()
//                    } catch (e: Throwable) {
//                        uncaughtExceptionHandler.uncaughtException(
//                            thread = this,
//                            throwable = RuntimeException("Error on network queue", e)
//                        )
//                    }
//                }
//                println("NetworkThread::$id-$name runnable executed=$runnable")
            }
        } catch (e: Throwable) {
            currentThread.uncaughtExceptionHandler.uncaughtException(this, e)
        } finally {
            closed.setValue(true)
            // selectedKeys.close() // TODO
        }
    }

    val isClosed
        get() = closed.getValue()

    override fun close() {
        closed.setValue(true)
    }

    fun closeAndJoin() {
        close()
        if (currentThread !== this && isActive) {
            join()
        }
    }
}
