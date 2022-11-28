package pw.binom.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ThreadRef
import pw.binom.io.Closeable
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext

abstract class NetworkCoroutineDispatcher : CoroutineDispatcher(), NetworkManager {
    companion object {
        fun create() = NetworkCoroutineDispatcherImpl()
        var default: NetworkCoroutineDispatcher = create()
    }

//    abstract suspend fun tcpConnect(address: NetworkAddress): TcpConnection
}

private var counter = 0

class NetworkCoroutineDispatcherImpl : NetworkCoroutineDispatcher(), Closeable {

    private var closed = AtomicBoolean(false)

    //    private var worker = Worker()
    private val selector = Selector.open()

    //    private val internalUdpChannel = UdpSocketChannel()
    override fun toString(): String = "Dispatchers.Network"

//    init {
//        internalUdpChannel.setBlocking(false)
//    }

    //    private val internalKey = selector.attach(internalUdpChannel)
    private val readyForWriteListener = BatchExchange<Runnable>()

    //    private val internalUdpContinuationConnection = attach(internalUdpChannel)
    private var networkThreadRef = ThreadRef()
    private val selectedKeys = SelectedEvents.create()

    val networkThread = Thread("NetworkThread-${counter++}") { thisThread ->
        try {
            while (!closed.getValue()) {
                this.networkThreadRef = ThreadRef()
                val count = this.selector.select(selectedEvents = selectedKeys)
                val iterator = selectedKeys.iterator()
                while (iterator.hasNext() && !this.closed.getValue()) {
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
                        thisThread.uncaughtExceptionHandler.uncaughtException(
                            thread = thisThread,
                            throwable = e,
                        )
                    }
                }

                readyForWriteListener.popAll {
                    it.forEach {
                        try {
                            it.run()
                        } catch (e: Throwable) {
                            thisThread.uncaughtExceptionHandler.uncaughtException(
                                thread = thisThread,
                                throwable = RuntimeException("Error on network queue", e)
                            )
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            thisThread.uncaughtExceptionHandler.uncaughtException(
                thread = thisThread,
                throwable = RuntimeException("Error on network queue", e)
            )
        } finally {
            freeResources()
        }
    }

    init {
        networkThread.start()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        readyForWriteListener.push(block)
        wakeup()
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = !networkThreadRef.same

    override fun wakeup() {
        selector.wakeup()
    }

    private fun freeResources() {
        selector.getAttachedKeys().toTypedArray().forEach {
            val attachment = it.attachment as AbstractConnection
            attachment.cancelSelector()
        }
        readyForWriteListener.clear()
        selector.close()
    }

    override fun close() {
        closed.setValue(true)
        wakeup()
        networkThread.join()
    }

    override fun attach(channel: UdpSocketChannel): UdpConnection {
        val con = UdpConnection(channel)
        channel.setBlocking(false)
        val key = selector.attach(channel, con, 0)
        con.keys.addKey(key)
        return con
    }

    override fun attach(channel: TcpClientSocketChannel, mode: Int): TcpConnection {
        val con = TcpConnection(channel)
        channel.setBlocking(false)
        val key = selector.attach(socket = channel, attachment = con, mode = mode)
        con.keys.addKey(key)
        return con
    }

    override fun attach(channel: TcpServerSocketChannel): TcpServerConnection {
        val con = TcpServerConnection(channel = channel, dispatcher = this)
        channel.setBlocking(false)
        con.keys.addKey(selector.attach(socket = channel, attachment = con, mode = 0))
        return con
    }
}

val Dispatchers.Network
    get() = NetworkCoroutineDispatcher.default
