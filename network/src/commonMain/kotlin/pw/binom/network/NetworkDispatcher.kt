package pw.binom.network

import pw.binom.BaseFuture
import pw.binom.Future2
import pw.binom.concurrency.*
import pw.binom.doFreeze
import pw.binom.io.Closeable
import kotlin.coroutines.*

class NetworkDispatcher : Closeable {
    val selector = Selector.open()
    private val wakeUpConnection = openUdp()
    internal val crossThreadWakeUpHolder = wakeUpConnection.holder

    class Awakener(val key: CrossThreadKeyHolder) {
        fun wakeup(func: (() -> Unit)? = null) {
            key.waitReadyForWrite { func?.invoke() }
        }

        init {
            doFreeze()
        }
    }

    /**
     * Special object for wakeup NetworkDispatcher in selecting mode from other thread.
     */
    val awakener = Awakener(wakeUpConnection.holder)

    fun<R> async(executor: WorkerPool? = null, func: suspend () -> R): Future2<R> {
        val future = BaseFuture<R>()
        func.startCoroutine(object : Continuation<R> {
            override val context: CoroutineContext = run {
                var ctx = EmptyCoroutineContext + NetworkDispatcherHolderElement(this@NetworkDispatcher)
                if (executor != null) {
                    ctx += ExecutorPoolHolderElement(executor)
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
            val connection = key.attachment as AbstractConnection
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
        suspendCoroutine<Unit> {
            connection.connect = it
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
        con.holder = CrossThreadKeyHolder(selector.attach(channel, 0, con))
        return con
    }

    fun attach(channel: TcpClientSocketChannel): TcpConnection {
        val con = TcpConnection(channel)
        con.holder = CrossThreadKeyHolder(selector.attach(channel, 0, con))
        return con
    }

    override fun close() {
        selector.close()
    }
}