package pw.binom.network

import kotlinx.coroutines.*
import pw.binom.PopResult
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.*
import pw.binom.doFreeze
import pw.binom.io.Closeable
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NetworkCoroutineDispatcher : CoroutineDispatcher(), NetworkManager, Closeable {

    companion object {
        var DEFAULT = NetworkCoroutineDispatcher()
    }

    private var closed by AtomicBoolean(false)
    private var worker = Worker.create()
    private val selector = Selector.open()
    private val internalUdpChannel = UdpSocketChannel()
    private val readyForWriteListener = ConcurrentQueue<() -> Unit>()
    private val internalUdpContinuationConnection = attach(internalUdpChannel)
//    private lateinit var aaKey:UdpConnection// = attach(internalUdpContinuation)

    //    private val internalContinuation =
//        CrossThreadKeyHolder(selector.attach(internalUdpContinuation, 0, internalUdpContinuation))
    private var networkThread: ThreadRef = ThreadRef()

    init {
        doFreeze()
        worker.execute(this) { self ->
            self.networkThread = ThreadRef()
            try {
                while (!self.closed) {
//                    println("Select....")
//                    println("selecting keys...")
                    val iterator = self.selector.select()
//                    println("selector done!")
                    while (iterator.hasNext() && !self.closed) {
                        val event = iterator.next()
                        val attachment = event.key.attachment
                        if (attachment === internalUdpContinuationConnection) {
                            println("Internal $event")
                            val crossThreadWaiterResultHolder = PopResult<() -> Unit>()
                            while (true) {
                                readyForWriteListener.pop(crossThreadWaiterResultHolder)
                                if (crossThreadWaiterResultHolder.isEmpty) {
                                    break
                                } else {
                                    crossThreadWaiterResultHolder.value()
                                }
                            }
                            internalUdpContinuationConnection.key.listensFlag = 0
                        } else {
                            println("External $event")
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
        readyForWriteListener.push {
            block.run()
        }
        internalUdpContinuationConnection.key.addListen(Selector.OUTPUT_READY)
    }

    override fun close() {
        closed = true
        internalUdpChannel.close()
        internalUdpContinuationConnection.key.close()
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

    suspend fun tcpConnect(address: NetworkAddress): TcpConnection =
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
                if (e.message != null) {
                    throw e
                } else {
                    throw SocketConnectException(address.toString(), e.cause)
                }
                connection.asyncClose()
            }
            connection
        }
}

suspend fun getDispatcher(): CoroutineDispatcher? =
    suspendCoroutine {
        it.resume(it.context[ContinuationInterceptor] as CoroutineDispatcher?)
    }

val Dispatchers.Network
    get() = NetworkCoroutineDispatcher.DEFAULT