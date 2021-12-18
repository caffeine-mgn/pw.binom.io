package pw.binom.network

import kotlinx.coroutines.*
import pw.binom.PopResult
import pw.binom.atomic.AtomicBoolean
import pw.binom.collection.LinkedList
import pw.binom.concurrency.*
import pw.binom.coroutine.getDispatcher
import pw.binom.io.Closeable
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resumeWithException
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
    private val readyForWriteListener = ArrayList<Runnable>()
    private val readyForWriteListenerLock = SpinLock()
    private val internalUdpContinuationConnection = attach(internalUdpChannel)
    private var networkThread = ThreadRef()

    init {
        worker.execute(this) { self ->
            try {
                while (!self.closed) {
                    self.networkThread = ThreadRef()
                    val iterator = self.selector.select()
                    var executeOnNetwork = false
                    while (iterator.hasNext() && !self.closed) {
                        val event = iterator.next()
                        val attachment = event.key.attachment
                        if (attachment === self.internalUdpContinuationConnection) {
                            executeOnNetwork = true
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
                    if (executeOnNetwork) {
                        self.readyForWriteListenerLock.synchronize {
                            if (readyForWriteListener.isEmpty()) {
                                self.internalUdpContinuationConnection.key.listensFlag = 0
                            } else {
                                readyForWriteListener.forEach {
                                    try {
                                        it.run()
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                    }
                                }
                                readyForWriteListener.clear()
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                self.close()
            }
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        readyForWriteListenerLock.synchronize {
            readyForWriteListener.add(block)
            internalUdpContinuationConnection.key.addListen(Selector.OUTPUT_READY)
        }
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean =
        !networkThread.same

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

    override fun attach(channel: TcpClientSocketChannel, mode: Int): TcpConnection {
        val con = TcpConnection(channel)
        val key = selector.attach(channel, mode, con)
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
            val connection = attach(channel, mode = Selector.EVENT_CONNECTED or Selector.EVENT_ERROR)
            try {
                connection.description = address.toString()
                suspendCancellableCoroutine<Unit> {
                    connection.connect = it
                    it.invokeOnCancellation {
                        connection.cancelSelector()
                    }
                    try {
                        channel.connect(address)
                    } catch (e: Throwable) {
                        it.resumeWithException(e)
                    }
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