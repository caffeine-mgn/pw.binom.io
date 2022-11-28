package pw.binom.network

import kotlinx.coroutines.Runnable
import pw.binom.BatchExchange
import pw.binom.LoopWatcher
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.thread.Thread

class NetworkThread : Thread, Closeable {
    private val selector: Selector
    private val readyForWriteListener: BatchExchange<Runnable>

    constructor(name: String, selector: Selector, readyForWriteListener: BatchExchange<Runnable>) : super(name = name) {
        this.selector = selector
        this.readyForWriteListener = readyForWriteListener
    }

    constructor(selector: Selector, readyForWriteListener: BatchExchange<Runnable>) : super() {
        this.selector = selector
        this.readyForWriteListener = readyForWriteListener
    }

    //    private val selector = Selector.open()
    private val selectedKeys = SelectedEvents.create()
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

    private val loopWatcher by lazy { LoopWatcher("NetworkThread-$name") }

    override fun execute() {
        try {
            while (!closed.getValue()) {
                loopWatcher.call()
                this.selector.select(selectedEvents = selectedKeys)
                val iterator = selectedKeys.iterator()
                while (iterator.hasNext() && !closed.getValue()) {
                    try {
                        val event = iterator.next()
                        val attachment = event.key.attachment
                        attachment ?: error("Attachment is null")
                        val connection = attachment as AbstractConnection
                        when {
                            event.mode and Selector.EVENT_CONNECTED != 0 -> connection.connected()
                            event.mode and Selector.EVENT_ERROR != 0 -> connection.error()
                            event.mode and Selector.OUTPUT_READY != 0 -> connection.readyForWrite(event.key)
                            event.mode and Selector.INPUT_READY != 0 -> connection.readyForRead(event.key)
                            else -> error("Unknown connection event")
                        }
                    } catch (e: Throwable) {
                        uncaughtExceptionHandler.uncaughtException(
                            thread = this,
                            throwable = e,
                        )
                    }
                }
                readyForWriteListener.popAll {
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
        } finally {
            loopWatcher.close()
            selectedKeys.close()
        }
    }

    val isClosed
        get() = closed.getValue()

    override fun close() {
        if (closed.getValue()) {
            return
        }
        closed.setValue(true)
    }

    fun closeAndJoin() {
        close()
        if (currentThread !== this && isActive) {
            join()
        }
    }
}
