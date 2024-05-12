package pw.binom.network

// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.Runnable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.io.socket.Selector
import pw.binom.thread.SingleThreadExecutorService
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext

abstract class NetworkCoroutineDispatcher : AbstractNetworkManager(), NetworkManager {
    companion object {
        fun create() = NetworkCoroutineDispatcherImpl()
        var default: NetworkCoroutineDispatcher = create()
    }

//    override val key: CoroutineContext.Key<*>
//        get() = NetworkInterceptor.Key

//    abstract suspend fun tcpConnect(address: NetworkAddress): TcpConnection
}

class NetworkCoroutineDispatcherImpl : NetworkCoroutineDispatcher(), Closeable {

    private val executor = SingleThreadExecutorService()

    private var closed = AtomicBoolean(false)
    override val selector = Selector()
    override fun ensureOpen() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

    private val selectThread = Thread {
        SelectExecutor.startSelecting(
            selector = selector,
            isSelectorClosed = { closed.getValue() },
            submitTask = executor::submit,
        )
    }

    init {
        selectThread.start()
    }

    override fun toString(): String = "Dispatchers.Network"

    //    private val exchange = Exchange<Runnable>()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (closed.getValue()) {
            return // skip any tasks after closed
        }
        executor.submit { block.run() }
        wakeup()
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean = !executor.isThreadFromPool(Thread.currentThread)

    override fun close() {
      println("NetworkCoroutineDispatcherImpl::close")
        if (closed.compareAndSet(false, true)) {
            executor.shutdownNow()
            selector.wakeup()
            selectThread.join()
            selector.close()
        }
    }
}

val Dispatchers.Network
    get() = NetworkCoroutineDispatcher.default
