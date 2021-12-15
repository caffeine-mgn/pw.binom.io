package pw.binom.network

import kotlinx.coroutines.*
import pw.binom.PopResult
import pw.binom.atomic.AtomicBoolean
import pw.binom.collection.LinkedList
import pw.binom.concurrency.*
import pw.binom.io.Closeable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

abstract class NetworkCoroutineDispatcher : CoroutineDispatcher(), NetworkManager {
    companion object {
        fun create() = NetworkCoroutineDispatcherImpl()
        var default: NetworkCoroutineDispatcher = create()
    }

    abstract suspend fun tcpConnect(address: NetworkAddress): TcpConnection
}

class NetworkCoroutineDispatcherImpl : NetworkCoroutineDispatcher(), Closeable {

    private var closed by AtomicBoolean(false)
    private var worker = Worker()
    private val selector = Selector.open()
    private val internalUdpChannel = UdpSocketChannel()
    private val readyForWriteListener = LinkedList<Runnable>()
    private val readyForWriteListenerLock = SpinLock()
    private val internalUdpContinuationConnection = attach(internalUdpChannel)

    init {
        worker.execute(this) { self ->
            try {
                while (!self.closed) {
                    val iterator = self.selector.select()
                    while (iterator.hasNext() && !self.closed) {
                        val event = iterator.next()
                        val attachment = event.key.attachment
                        if (attachment === self.internalUdpContinuationConnection) {
                            self.readyForWriteListenerLock.synchronize {
                                while (self.readyForWriteListener.isNotEmpty()) {
                                    self.readyForWriteListener.removeLast().run()
                                }
                            }

//                                while (true) {
//                                    val breakNeed = readyForWriteListenerLock.synchronize {
//                                        if (readyForWriteListener.isEmpty()) {
//                                            true
//                                        } else {
//                                            readyForWriteListener.removeLast().run()
//                                            false
//                                        }
//                                    }
//                                    if (breakNeed){
//                                        break
//                                    }
//                                }
                            self.internalUdpContinuationConnection.key.listensFlag = 0
                        } else {
                            val connection = attachment as AbstractConnection
                            if (event.mode and Selector.EVENT_CONNECTED != 0) {
                                connection.connected()
                            }
                            if (event.mode and Selector.EVENT_ERROR != 0) {
                                connection.error()
                            }
                            if (event.mode and Selector.OUTPUT_READY != 0) {
                                connection.readyForWrite()
                            }
                            if (event.mode and Selector.INPUT_READY != 0) {
                                connection.readyForRead()
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
//        readyForWriteListenerLock.synchronize {
            readyForWriteListener.addFirst(block)
//        }
        internalUdpContinuationConnection.key.addListen(Selector.OUTPUT_READY)
    }

    override fun close() {
        closed = true
        internalUdpContinuationConnection.key.close()
        internalUdpChannel.close()

        selector.getAttachedKeys().forEach {
            val attachment = it.attachment as AbstractConnection
            attachment.cancelSelector()
        }

        selector.close()
    }

    override fun attach(channel: UdpSocketChannel): UdpConnection {
        val con = UdpConnection(channel)
        val key = selector.attach(channel, 0, con)
        con.key = key
        return con
    }

    override fun attach(channel: TcpClientSocketChannel): TcpConnection {
        val con = TcpConnection(channel)
        val key = selector.attach(channel, 0, con)
        con.key = key
        return con
    }

    override fun attach(channel: TcpServerSocketChannel): TcpServerConnection {
        val con = TcpServerConnection(this, channel)
        con.key = selector.attach(channel, 0, con)
        return con
    }

    override suspend fun tcpConnect(address: NetworkAddress): TcpConnection =
        withContext(this) {
            val channel = TcpClientSocketChannel()
            val connection = attach(channel)
            channel.connect(address)
            connection.connecting()
            try {
                suspendCoroutine<Unit> {
                    connection.connect = it
                }
            } catch (e: SocketConnectException) {
                runCatching { connection.asyncClose() }
                if (e.message != null) {
                    throw e
                } else {
                    throw SocketConnectException(address.toString(), e.cause)
                }
            }
            connection
        }
}

val Dispatchers.Network
    get() = NetworkCoroutineDispatcher.default