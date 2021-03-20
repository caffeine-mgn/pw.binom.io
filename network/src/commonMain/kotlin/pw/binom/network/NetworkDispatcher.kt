package pw.binom.network

import pw.binom.NonFreezableFuture
import pw.binom.PopResult
import pw.binom.concurrency.*
import pw.binom.io.Closeable
import kotlin.coroutines.*

class NetworkDispatcher : Closeable {
    private val selector = Selector.open()
    private val internalUdpContinuation = UdpSocketChannel()
    private val internalContinuation =
        CrossThreadKeyHolder(selector.attach(internalUdpContinuation, 0, internalUdpContinuation))
    internal val crossThreadWakeUpHolder = internalContinuation
    private val crossThreadWaiterResultHolder = PopResult<() -> Unit>()

    suspend fun yield() {
        suspendCoroutine<Unit> {
            internalContinuation.waitReadyForWrite { it.resume(Unit) }
        }
    }

    fun <R> async(executor: ExecutorService? = null, func: suspend () -> R): NonFreezableFuture<R> {
        val future = NonFreezableFuture<R>()
        func.startCoroutine(object : Continuation<R> {
            override val context: CoroutineContext = run {
                var ctx =
                    CrossThreadCoroutineElement(this@NetworkDispatcher.crossThreadWakeUpHolder) +
                            NetworkHolderElement(this@NetworkDispatcher.crossThreadWakeUpHolder)
                if (executor != null) {
                    ctx += ExecutorServiceHolderElement(executor)
                }
                ctx
            }

            override fun resumeWith(result: Result<R>) {
                future.resume(result)
            }
        })
        return future
    }

    fun select(timeout: Long = -1L) =
        selector.select(timeout) { key, mode ->
            val attachment = key.attachment
            if (attachment === internalUdpContinuation) {
                while (true) {
                    internalContinuation.readyForWriteListener.pop(crossThreadWaiterResultHolder)
                    if (crossThreadWaiterResultHolder.isEmpty) {
                        break
                    } else {
                        crossThreadWaiterResultHolder.value()
                    }
                }
                internalContinuation.key.listensFlag = 0
                return@select
            }
            val connection = attachment as AbstractConnection

            if (mode and Selector.EVENT_CONNECTED != 0) {
                connection.connected()
            }
            if (mode and Selector.EVENT_ERROR != 0) {
                connection.error()
            }
            if (mode and Selector.OUTPUT_READY != 0) {
                connection.readyForWrite()
            }
            if (mode and Selector.INPUT_READY != 0) {
                connection.readyForRead()
            }
        }

    suspend fun tcpConnect(address: NetworkAddress): TcpConnection {
        val channel = TcpClientSocketChannel()
        channel.connect(address)
        val connection = attach(channel)
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
        }
        return connection
    }

    fun bindTcp(address: NetworkAddress): TcpServerConnection {
        val channel = TcpServerSocketChannel()
        channel.bind(address)
        return attach(channel)
    }

    fun attach(channel: TcpServerSocketChannel): TcpServerConnection {
        val con = TcpServerConnection(this, channel)
        con.key = selector.attach(channel, 0, con)
        return con
    }

    fun bindUDP(address: NetworkAddress): UdpConnection {
        val channel = UdpSocketChannel()
        channel.bind(address)
        return attach(channel)
    }

    fun openUdp(): UdpConnection {
        val channel = UdpSocketChannel()
        return attach(channel)
    }

    fun attach(channel: UdpSocketChannel): UdpConnection {
        val con = UdpConnection(channel)
        val key = selector.attach(channel, 0, con)
        con.key = key
        return con
    }

    fun attach(channel: TcpClientSocketChannel): TcpConnection {
        val con = TcpConnection(channel)
        val key = selector.attach(channel, 0, con)
        con.key = key
        return con
    }

    override fun close() {
        internalUdpContinuation.close()
        internalContinuation.close()
        selector.close()
    }
}