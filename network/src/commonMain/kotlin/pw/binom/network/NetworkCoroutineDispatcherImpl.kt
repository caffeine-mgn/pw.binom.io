package pw.binom.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ThreadRef
import pw.binom.concurrency.Worker
import pw.binom.io.Closeable
import kotlin.coroutines.CoroutineContext

abstract class NetworkCoroutineDispatcher : CoroutineDispatcher(), NetworkManager {
    companion object {
        fun create() = NetworkCoroutineDispatcherImpl()
        var default: NetworkCoroutineDispatcher = create()
    }

//    abstract suspend fun tcpConnect(address: NetworkAddress): TcpConnection
}

class NetworkCoroutineDispatcherImpl : NetworkCoroutineDispatcher(), Closeable {

    private var closed = AtomicBoolean(false)
    private var worker = Worker()
    private val selector = Selector.open()
    private val internalUdpChannel = UdpSocketChannel()

    init {
        internalUdpChannel.setBlocking(false)
    }

    private val internalKey = selector.attach(internalUdpChannel)
    private val readyForWriteListener = BatchExchange<Runnable>()

    //    private val internalUdpContinuationConnection = attach(internalUdpChannel)
    private var networkThread = ThreadRef()
    private val selectedKeys = SelectedEvents.create()

    init {
        worker.execute(this) { self ->
            try {
                while (!self.closed.getValue()) {
                    self.networkThread = ThreadRef()
                    self.selector.select(selectedEvents = selectedKeys)
                    val iterator = selectedKeys.iterator()
                    var executeOnNetwork = false
                    while (iterator.hasNext() && !self.closed.getValue()) {
                        val event = iterator.next()
                        if (event.key === self.internalKey) {
                            executeOnNetwork = true
                        } else {
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
                        }
                    }
                    if (executeOnNetwork) {
                        if (self.readyForWriteListener.isEmpty()) {
                            self.internalKey.listensFlag = 0
                        } else {
                            self.readyForWriteListener.popAll {
                                it.forEach {
                                    try {
                                        it.run()
                                    } catch (e: Throwable) {
                                        println("Error on ROOT NetworkDispatcher #2")
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                println("Error on ROOT NetworkDispatcher #1")
                e.printStackTrace()
            } finally {
                self.close()
            }
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        readyForWriteListener.push(block)
        wakeup()
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean =
        !networkThread.same

    override fun wakeup() {
        try {
            internalKey.addListen(Selector.OUTPUT_READY)
        } catch (e: Throwable) {
            throw IllegalStateException("Can't switch on internal udp interrupt", e)
        }
    }

    override fun close() {
        closed.setValue(true)
        internalKey.close()
        internalUdpChannel.close()

        selector.getAttachedKeys().forEach {
            val attachment = it.attachment as AbstractConnection
            attachment.cancelSelector()
        }

        selector.close()
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
