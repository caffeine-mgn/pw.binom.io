package pw.binom.network

/*
class NetworkDispatcher : Dispatcher, Closeable {
    private val networkThread = ThreadRef()
    private val network = Reference(NetworkImpl())
    private val c = network.value.crossThreadWakeUpHolder

    init {
        doFreeze()
    }

    fun <T> startCoroutine(onDone: (Result<T>) -> Unit, context: CoroutineContext, func: suspend () -> T) {
        val dispatchContextElement = DispatcherCoroutineElement(this)
            .doFreeze()
        c.waitReadyForWrite {
            func.startCoroutine(object : Continuation<T> {
                val dispatcherElement = dispatchContextElement
                override val context: CoroutineContext
                    get() = context + dispatcherElement

                override fun resumeWith(result: Result<T>) {
                    onDone(result)
                }
            })
        }
    }

    override fun <T> startCoroutine(context: CoroutineContext, func: suspend () -> T): FreezableFuture<T> {
        val future = FreezableFuture<T>()
        startCoroutine(
            context = context,
            func = func,
            onDone = {
                try {
                    future.resume(it)
                } catch (e: Throwable) {
                    if (it.isFailure) {
                        e.addSuppressed(it.exceptionOrNull()!!)
                    }
                    throw e
                }
            }
        )
        return future
    }

    override fun <T> startCoroutine(
        context: CoroutineContext,
        continuation: CrossThreadContinuation<T>,
        func: suspend () -> T
    ) {
        startCoroutine(
            context = context,
            func = func,
            onDone = {
                continuation.resumeWith(it)
            }
        )
    }

    private fun checkNetworkThread() {
        if (!networkThread.same) {
            throw IllegalStateException("You should call NetworkDispatcher from network thread")
        }
    }

    override fun <T> resume(continuation: Reference<Continuation<T>>, result: Result<T>) {
        c.coroutine(
            result = result,
            continuation = continuation as Reference<Continuation<Any?>>
        )
    }

    fun bindTcp(address: NetworkAddress): TcpServerConnection {
        checkNetworkThread()
        return network.value.bindTcp(address)
    }

    fun attach(channel: TcpServerSocketChannel): TcpServerConnection {
        checkNetworkThread()
        return network.value.attach(channel)
    }

    fun bindUDP(address: NetworkAddress): UdpConnection {
        checkNetworkThread()
        return network.value.bindUDP(address)
    }

    fun openUdp(): UdpConnection {
        checkNetworkThread()
        return network.value.openUdp()
    }

    fun attach(channel: UdpSocketChannel): UdpConnection {
        checkNetworkThread()
        return network.value.attach(channel)
    }

    suspend fun tcpConnect(address: NetworkAddress): TcpConnection {
        checkNetworkThread()
        return network.value.tcpConnect(address)
    }

    suspend fun yield() {
        checkNetworkThread()
        network.value.yield()
    }

    fun attach(channel: TcpClientSocketChannel): TcpConnection {
        checkNetworkThread()
        return network.value.attach(channel)
    }

    override fun close() {
        checkNetworkThread()
        network.value.close()
        network.close()
    }

    fun select(timeout: Long = -1L) = network.value.select(timeout)
    fun <T> runSingle(func: suspend () -> T): T {
        checkNetworkThread()
        val network=network.value
        val r = startCoroutine(func = func)
        while (!r.isDone) {
            network.select()
        }
        return r.joinAndGetOrThrow()
    }
}
*/
