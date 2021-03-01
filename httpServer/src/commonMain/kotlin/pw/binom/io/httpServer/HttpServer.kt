package pw.binom.io.httpServer

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.async
import pw.binom.concurrency.WorkerPool
import pw.binom.concurrency.asyncWithExecutor
import pw.binom.io.AsyncBufferedAsciiInputReader
import pw.binom.io.Closeable
import pw.binom.network.*
import pw.binom.pool.DefaultPool

/**
 * Base Http Server
 *
 * @param handler request handler
 * @param zlibBufferSize size of zlib buffer. 0 - disable zlib
 */
open class HttpServer(
    val manager: NetworkDispatcher,
    protected val handler: Handler,
    poolSize: Int = 10,
    val executor: WorkerPool? = null,
    inputBufferSize: Int = DEFAULT_BUFFER_SIZE,
    outputBufferSize: Int = DEFAULT_BUFFER_SIZE,
    private val zlibBufferSize: Int = DEFAULT_BUFFER_SIZE
) : Closeable {

    init {
        require(zlibBufferSize >= 0) { "zlibBufferSize must be grate or equals than 0" }
    }

    private val returnToPoolForOutput: (NoCloseOutput) -> Unit = {
        outputBufferPool.recycle(it)
    }

    private val bufferedInputPool = DefaultPool(poolSize) {
        val p = PooledAsyncBufferedInput(inputBufferSize)
        p to AsyncBufferedAsciiInputReader(p)
    }

    private val bufferedOutputPool = DefaultPool(poolSize) {
        PoolAsyncBufferedOutput(outputBufferSize)
    }

    private val outputBufferPool = DefaultPool(poolSize) {
        NoCloseOutput(returnToPoolForOutput)
    }
    private val httpRequestPool = DefaultPool(poolSize) {
        HttpRequestImpl2()
    }

    private val httpResponseBodyPool = DefaultPool(poolSize) {
        HttpResponseBodyImpl2()
    }

    private val httpResponsePool = DefaultPool(poolSize) {
        HttpResponseImpl2(httpResponseBodyPool, zlibBufferSize)
    }

    private suspend fun runProcessing(connection: TcpConnection) {
        val it = connection
        val inputBufferid = bufferedInputPool.borrow { buf ->
            buf.first.currentStream = it
        }
        val outputBufferid = bufferedOutputPool.borrow { buf ->
            buf.currentStream = it
        }
        while (true) {
            try {
                val keepAlive = ConnectionProcessing.process(
                    handler = this.handler,
                    httpRequestPool = httpRequestPool,
                    httpResponsePool = httpResponsePool,
                    asciiInputReader = inputBufferid.second,
                    outputBuffered = outputBufferid,
                    allowZlib = zlibBufferSize > 0,
                    rawConnection = it
                )
                if (!keepAlive) {
                    inputBufferid.first.reset()
                    inputBufferid.second.reset()
                    bufferedInputPool.recycle(inputBufferid)
                    outputBufferid.reset()
                    bufferedOutputPool.recycle(outputBufferid)
                    it.close()
                    break
                }
            } catch (e: SocketClosedException) {
                break
            } catch (e: Throwable) {
                e.printStackTrace()
                inputBufferid.first.reset()
                inputBufferid.second.reset()
                bufferedInputPool.recycle(inputBufferid)
                outputBufferid.reset()
                bufferedOutputPool.recycle(outputBufferid)
                it.close()
                break
            }
        }
    }

    private suspend fun clientConnected(connection: TcpConnection, manager: NetworkDispatcher) {
        runProcessing(connection)
    }

    private val binded = ArrayList<TcpServerConnection>()

    override fun close() {
        binded.forEach {
            it.close()
        }
        manager.async {
            while (bufferedInputPool.size > 0) {
                bufferedInputPool.borrow {
                    it.first.reset()
                }.let {
                    it.second.asyncClose()
                }
            }
        }
    }

    /**
     * Bind HTTP server to port [port]
     *
     * @param address Address for bind
     */
    fun bindHTTP(address:NetworkAddress) {
        val connect = manager.bindTcp(address)
        binded += connect
        manager.async {
            while (true) {
                val client = connect.accept() ?: continue
                manager.async(executor) {
                    clientConnected(client, manager)
                }
            }
        }
    }

//    fun bindHTTPS(ssl: SSLContext, host: String = "0.0.0.0", port: Int) {
//        binded += manager.bind(host = host, port = port, handler = this, factory = ssl.socketFactory)
//    }
}