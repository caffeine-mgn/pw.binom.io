package pw.binom.network

import kotlinx.coroutines.Runnable
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.Exchange
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.io.socket.KeyListenFlags
import pw.binom.io.socket.SelectedKeys
import pw.binom.io.socket.Selector
import pw.binom.thread.Thread
import kotlin.time.Duration

open class NetworkThread : Thread, Closeable {
    private val selector: Selector
    private val exchange: Exchange<Runnable>

    constructor(name: String, selector: Selector, exchange: Exchange<Runnable>) : super(name = name) {
        this.selector = selector
        this.exchange = exchange
    }

    constructor(selector: Selector, exchange: Exchange<Runnable>) : super() {
        this.selector = selector
        this.exchange = exchange
    }

    //    private val selector = Selector.open()
    private val selectedKeys = SelectedKeys()
    private val closed = AtomicBoolean(false)
    private fun checkClosed() {
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

    override fun execute() {
        try {
            while (!closed.getValue()) {
//                loopWatcher.call()
//                var taskForRun = defaultMutableList<Runnable>()
                var v = 0
                this.selector.select(timeout = Duration.INFINITE, selectedKeys = selectedKeys)
                selectedKeys.forEach { event ->
                    v++
                    try {
                        val attachment = event.key.attachment
                        attachment ?: error("Attachment is null")
                        val connection = attachment as AbstractConnection
                        when {
//                            event.flags and SelectorOld.EVENT_CONNECTED != 0 -> connection.connected()
                            event.flags and KeyListenFlags.ERROR != 0 -> connection.error()
                            event.flags and KeyListenFlags.WRITE != 0 -> connection.readyForWrite(event.key)
                            event.flags and KeyListenFlags.READ != 0 -> connection.readyForRead(event.key)
                            else -> error("Unknown connection event")
                        }
                    } catch (e: Throwable) {
                        uncaughtExceptionHandler.uncaughtException(
                            thread = this,
                            throwable = e,
                        )
                    }
                }
                val runnable = exchange.getOrNull()
                if (runnable != null) {
                    try {
                        runnable.run()
                    } catch (e: Throwable) {
                        uncaughtExceptionHandler.uncaughtException(
                            thread = this,
                            throwable = RuntimeException("Error on network queue", e)
                        )
                    }
                }
//                taskForRun.forEach {
//                    try {
//                        it.run()
//                    } catch (e: Throwable) {
//                        uncaughtExceptionHandler.uncaughtException(
//                            thread = this,
//                            throwable = RuntimeException("Error on network queue", e)
//                        )
//                    }
//                }
//                taskForRun.clear()
            }
        } finally {
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
