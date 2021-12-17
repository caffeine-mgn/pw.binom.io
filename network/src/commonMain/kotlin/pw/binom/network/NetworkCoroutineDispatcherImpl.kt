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
    private val readyForWriteListener = LinkedList<Runnable>()
    private val readyForWriteListenerLock = ReentrantSpinLock()
    private val internalUdpContinuationConnection = attach(internalUdpChannel)

    init {
        worker.execute(this) { self ->
            try {
                while (!self.closed) {
                    val iterator = self.selector.select()
                    while (iterator.hasNext() && !self.closed) {
                        val event = iterator.next()
                        println("select #1 $event  ${event.key.attachment}")
                        println("select #1")
                        val attachment = event.key.attachment
                        if (attachment === self.internalUdpContinuationConnection) {
                            self.readyForWriteListenerLock.synchronize {
                                while (self.readyForWriteListener.isNotEmpty()) {
                                    try {
                                        println("execute on network #1")
                                        self.readyForWriteListener.removeLast().run()
                                        println("execute on network #2")
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                        throw e
                                    }
                                }
                                self.internalUdpContinuationConnection.key.listensFlag = 0
                            }
                        } else {
                            val connection = attachment as AbstractConnection
                            if (event.mode and Selector.EVENT_CONNECTED != 0) {
                                println("Call connected")
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
            } finally {
                println("Network manager closed")
                self.close()
            }
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (context[ContinuationInterceptor] === this) {
            block.run()
        } else {
            readyForWriteListenerLock.synchronize {
                readyForWriteListener.addFirst(block)
                internalUdpContinuationConnection.key.addListen(Selector.OUTPUT_READY)
            }
        }
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

            try {
                println("Connecting...")

                suspendCancellableCoroutine<Unit> {
                    connection.connect = it
                    it.invokeOnCancellation {
                        println("Connect canceled!")
                        connection.cancelSelector()
                    }
                    try {
                        channel.connect(address)
                        connection.connecting()
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