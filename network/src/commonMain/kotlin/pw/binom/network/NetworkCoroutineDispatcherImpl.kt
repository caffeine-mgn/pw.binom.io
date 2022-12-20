package pw.binom.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.io.socket.Selector
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext

abstract class NetworkCoroutineDispatcher : AbstractNetworkManager(), NetworkManager {
    companion object {
        fun create() = NetworkCoroutineDispatcherImpl()
        var default: NetworkCoroutineDispatcher = create()
    }

//    abstract suspend fun tcpConnect(address: NetworkAddress): TcpConnection
}

private var counter = 0

class NetworkCoroutineDispatcherImpl : NetworkCoroutineDispatcher(), Closeable {

    private var closed = AtomicBoolean(false)
    override val selector = Selector()
    override fun ensureOpen() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

    override fun toString(): String = "Dispatchers.Network"

//    private val exchange = Exchange<Runnable>()
    private val exchange = BatchExchange<Runnable>()
    val networkThread = NetworkThread(
        selector = selector,
        exchange = this.exchange,
        name = "NetworkThread-${counter++}"
    )

    init {
        networkThread.start()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        this.exchange.push(block)
        wakeup()
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = Thread.currentThread.id == networkThread.id

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            networkThread.close()
            selector.wakeup()
            networkThread.join()
            selector.close()
        }
    }
}

val Dispatchers.Network
    get() = NetworkCoroutineDispatcher.default
