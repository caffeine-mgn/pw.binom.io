package pw.binom.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableList
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext

class MultiFixedSizeThreadNetworkDispatcher(threadSize: Int) : CoroutineDispatcher(), NetworkManager, Closeable {
    private val selector = Selector.open()
    private val readyForWriteListener = BatchExchange<Runnable>()

    init {
        require(threadSize > 0) { "threadSize should be more than 0" }
    }

    private val closed = AtomicBoolean(false)
    private fun checkClosed() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

    private val threads = defaultMutableList<NetworkThread>(threadSize)

    init {
        repeat(threadSize) {
            val thread = NetworkThread(
                selector = selector,
                readyForWriteListener = readyForWriteListener,
                name = "NetworkDispatcher-$it"
            )
            thread.start()
            threads += thread
        }
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        checkClosed()
        val currentId = Thread.currentThread.id
        return !threads.any { it.id == currentId }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        checkClosed()
//        var min = threads[0]
//        for (i in 1 until threads.size) {
//            val item = threads[i]
//            if (item.taskSize < min.taskSize) {
//                min = item
//            }
//        }
        readyForWriteListener.push(block)
        selector.wakeup()
//        min.executeOnThread(block)
//        min.wakeup()
    }

    override fun attach(channel: TcpClientSocketChannel, mode: Int): TcpConnection {
        checkClosed()
        val con = TcpConnection(channel)
        channel.setBlocking(false)
        val key = selector.attach(
            socket = channel,
            attachment = con,
            mode = mode,
        )
        con.keys.addKey(key)
        if (mode != 0) {
            selector.wakeup()
        }
        return con
    }

    override fun attach(channel: TcpServerSocketChannel): TcpServerConnection {
        checkClosed()
        val con = TcpServerConnection(channel = channel, dispatcher = this)
        channel.setBlocking(false)
        val key = selector.attach(
            socket = channel,
            attachment = con,
            mode = 0,
        )
        con.keys.addKey(key)
        return con
    }

    override fun attach(channel: UdpSocketChannel): UdpConnection {
        checkClosed()
        val con = UdpConnection(channel)
        channel.setBlocking(false)
        val key = selector.attach(
            socket = channel,
            attachment = con,
            mode = 0,
        )
        con.keys.addKey(key)
        return con
    }

    override fun wakeup() {
        checkClosed()
        selector.wakeup()
    }

    override fun close() {
        if (closed.getValue()) {
            return
        }
        threads.forEach {
            it.close()
        }
        while (threads.isNotEmpty()) {
            threads.removeIf { !it.isActive }
            selector.wakeup()
            Thread.sleep(10)
        }
        selector.close()
    }
}
