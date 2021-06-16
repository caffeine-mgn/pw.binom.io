package pw.binom.io.httpServer

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.concurrency.WorkerPool
import pw.binom.io.AsyncBufferedAsciiInputReader
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.network.*
import pw.binom.pool.DefaultPool

///**
// * Base Http Server
// *
// * @param handler request handler
// * @param zlibBufferSize size of zlib buffer. 0 - disable zlib
// */
//@Deprecated(message = "Use HttpServer")
//open class HttpServer3(
//    val manager: NetworkDispatcher,
//    protected val handler: Handler3Deprecated,
//    poolSize: Int = 10,
//    val executor: WorkerPool? = null,
//    inputBufferSize: Int = DEFAULT_BUFFER_SIZE,
//    outputBufferSize: Int = DEFAULT_BUFFER_SIZE,
//    private val zlibBufferSize: Int = DEFAULT_BUFFER_SIZE
//) : Closeable {
//    init {
//        require(zlibBufferSize >= 0) { "zlibBufferSize must be grate or equals than 0" }
//    }
//
//    private var closed = false
//
//    private val returnToPoolForOutput: (NoCloseOutputDeprecated) -> Unit = {
//        outputBufferPool.recycle(it)
//    }
//
//    private val bufferedInputPool = DefaultPool(poolSize) {
//        val p = PooledAsyncBufferedInputDeprecated(inputBufferSize)
//        p to AsyncBufferedAsciiInputReader(p)
//    }
//
//    private val bufferedOutputPool = DefaultPool(poolSize) {
//        PoolAsyncBufferedOutputDeprecated(outputBufferSize)
//    }
//
//    private val outputBufferPool = DefaultPool(poolSize) {
//        NoCloseOutputDeprecated(returnToPoolForOutput)
//    }
//    private val httpRequestPool = DefaultPool(poolSize) {
//        HttpRequestImpl2Deprecated()
//    }
//
//    private val httpResponseBodyPool = DefaultPool(poolSize) {
//        HttpResponseBodyImpl2()
//    }
//
//    private val httpResponsePool = DefaultPool(poolSize) {
//        HttpResponseImpl2Deprecated(httpResponseBodyPool, zlibBufferSize)
//    }
//
//    private suspend fun runProcessing(connection: TcpConnection) {
//        val it = connection
//        val rawInputBuffered = bufferedInputPool.borrow { buf ->
//            buf.first.currentStream = it
//        }
//        val rawOutputBuffered = bufferedOutputPool.borrow { buf ->
//            buf.currentStream = it
//        }
//        try {
//            while (true) {
//                if (closed) {
//                    runCatching { connection.asyncClose() }
//                    break
//                }
//                try {
//                    val keepAlive = ConnectionProcessing.process(
//                        handler = this.handler,
//                        httpRequestPool = httpRequestPool,
//                        httpResponsePool = httpResponsePool,
//                        asciiInputReader = rawInputBuffered.second,
//                        outputBuffered = rawOutputBuffered,
//                        allowZlib = zlibBufferSize > 0,
//                        rawConnection = it
//                    )
//                    if (!keepAlive) {
//                        rawInputBuffered.first.reset()
//                        rawInputBuffered.second.reset()
//                        bufferedInputPool.recycle(rawInputBuffered)
//                        rawOutputBuffered.reset()
//                        bufferedOutputPool.recycle(rawOutputBuffered)
//                        it.close()
//                        break
//                    }
//                } catch (e: SocketClosedException) {
//                    break
//                } catch (e: Throwable) {
//                    runCatching { connection.asyncClose() }
//                    break
//                }
//            }
//        } finally {
//            rawInputBuffered.first.reset()
//            rawInputBuffered.second.reset()
//            bufferedInputPool.recycle(rawInputBuffered)
//            rawOutputBuffered.reset()
//            bufferedOutputPool.recycle(rawOutputBuffered)
//        }
//    }
//
//    private suspend fun clientConnected(connection: TcpConnection, manager: NetworkDispatcher) {
//        runProcessing(connection)
//    }
//
//    private val binded = ArrayList<TcpServerConnection>()
//
//    private fun checkClosed() {
//        check(!closed) { throw ClosedException() }
//    }
//
//    override fun close() {
//        checkClosed()
//        closed = true
//        binded.forEach {
//            it.close()
//        }
//        manager.async {
//            while (bufferedInputPool.size > 0) {
//                bufferedInputPool.borrow {
//                    it.first.reset()
//                }.let {
//                    it.second.asyncClose()
//                }
//            }
//        }
//    }
//
//    /**
//     * Bind HTTP server to port [port]
//     *
//     * @param address Address for bind
//     */
//    fun bindHTTP(address: NetworkAddress) {
//        checkClosed()
//        val connect = manager.bindTcp(address)
//        binded += connect
//        manager.async {
//            while (!closed) {
//                val client = connect.accept() ?: continue
//                manager.async(executor) {
//                    clientConnected(client, manager)
//                }
//            }
//        }
//    }
//}