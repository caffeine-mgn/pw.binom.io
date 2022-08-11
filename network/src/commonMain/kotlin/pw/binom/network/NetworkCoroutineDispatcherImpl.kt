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
                        attachment ?: throw IllegalStateException("Attachment is null")
                        val connection = attachment as AbstractConnection
                        when {
                            event.mode and Selector.EVENT_CONNECTED != 0 -> connection.connected()
                            event.mode and Selector.EVENT_ERROR != 0 -> connection.error()
                            event.mode and Selector.OUTPUT_READY != 0 -> connection.readyForWrite()
                            event.mode and Selector.INPUT_READY != 0 -> connection.readyForRead()
                            else -> throw IllegalStateException("Unknown connection event")
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
        selector.getAttachedKeys().forEach {
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
        val key = selector.attach(channel, 0, con)
        con.key = key
        return con
    }

    override fun attach(channel: TcpClientSocketChannel, mode: Int): TcpConnection {
        val con = TcpConnection(channel)
        channel.setBlocking(false)
        val key = selector.attach(channel, mode, con)
        con.key = key
        return con
    }

    override fun attach(channel: TcpServerSocketChannel): TcpServerConnection {
        val con = TcpServerConnection(this, channel)
        channel.setBlocking(false)
        con.key = selector.attach(channel, 0, con)
        return con
    }

//    override suspend fun tcpConnect(address: NetworkAddress): TcpConnection =
//        withContext(this) {
//            val channel = TcpClientSocketChannel()
//            val connection = attach(channel, mode = Selector.EVENT_CONNECTED or Selector.EVENT_ERROR)
//            try {
//                connection.description = address.toString()
//                suspendCancellableCoroutine<Unit> {
//                    connection.connect = it
//                    it.invokeOnCancellation {
//                        connection.cancelSelector()
//                        connection.close()
//                    }
//                    try {
// //                        connection.connecting()
//                        channel.connect(address)
//                    } catch (e: Throwable) {
//                        it.resumeWithException(e)
//                    }
//                }
//            } catch (e: SocketConnectException) {
//                runCatching { connection.asyncClose() }
//                if (e.message != null) {
//                    throw e
//                } else {
//                    throw SocketConnectException(address.toString(), e.cause)
//                }
//            }
//            connection
//        }
}

val Dispatchers.Network
    get() = NetworkCoroutineDispatcher.default
