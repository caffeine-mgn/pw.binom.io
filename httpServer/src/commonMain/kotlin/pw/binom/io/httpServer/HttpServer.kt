package pw.binom.io.httpServer

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.async
import pw.binom.io.Closeable
import pw.binom.io.socket.ServerSocketChannel
import pw.binom.io.socket.SocketClosedException
import pw.binom.io.socket.SocketFactory
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.socket.rawSocketFactory
import pw.binom.pool.DefaultPool
import pw.binom.stackTrace

/**
 * Base Http Server
 *
 * @param handler request handler
 * @param zlibBufferSize size of zlib buffer. 0 - disable zlib
 */
open class HttpServer(val manager: SocketNIOManager,
                      protected val handler: Handler,
                      poolSize: Int = 10,
                      inputBufferSize: Int = DEFAULT_BUFFER_SIZE,
                      outputBufferSize: Int = DEFAULT_BUFFER_SIZE,
                      private val zlibBufferSize: Int = DEFAULT_BUFFER_SIZE
) : Closeable, SocketNIOManager.ConnectHandler {

    init {
        require(zlibBufferSize >= 0) { "zlibBufferSize must be grate or equals than 0" }
    }

    private val returnToPoolForOutput: (NoCloseOutput) -> Unit = {
        outputBufferPool.recycle(it)
    }

    private val bufferedInputPool = DefaultPool(poolSize) {
        PooledAsyncBufferedInput(inputBufferSize)
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

    private fun runProcessing(connection: SocketNIOManager.ConnectionRaw, state: HttpConnectionState?, handler: ((req: HttpRequest, resp: HttpResponse) -> Unit)?) {
        connection {
            val inputBufferid = bufferedInputPool.borrow { buf ->
                buf.currentStream = it
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
                            inputBuffered = inputBufferid,
                            outputBuffered = outputBufferid,
                            allowZlib = zlibBufferSize > 0
                    )
                    if (!keepAlive) {
                        inputBufferid.reset()
                        bufferedInputPool.recycle(inputBufferid)
                        outputBufferid.reset()
                        bufferedOutputPool.recycle(outputBufferid)
                        it.close()
                        break
                    }
                } catch (e: SocketClosedException) {
                    break
                } catch (e: Throwable) {
                    println("Error: $e")
                    e.stackTrace.forEach {
                        println(it)
                    }
                    inputBufferid.reset()
                    bufferedInputPool.recycle(inputBufferid)
                    outputBufferid.reset()
                    bufferedOutputPool.recycle(outputBufferid)
                    it.close()
                    break
                }
            }
        }
    }

    override fun clientConnected(connection: SocketNIOManager.ConnectionRaw, manager: SocketNIOManager) {
        runProcessing(connection, null, null)
    }

    private val binded = ArrayList<ServerSocketChannel>()

    override fun close() {
        binded.forEach {
            it.close()
        }
        manager.close()
        async {
            while (bufferedInputPool.size > 0) {
                bufferedInputPool.borrow { it.currentStream = null }.close()
            }
        }
    }

    /**
     * Bind HTTP server to port [port]
     *
     * @param port Port for bind
     */
    fun bindHTTP(host: String = "0.0.0.0", port: Int) {
        binded += manager.bind(host = host, port = port, handler = this, factory = SocketFactory.rawSocketFactory)
    }

//    fun bindHTTPS(ssl: SSLContext, host: String = "0.0.0.0", port: Int) {
//        binded += manager.bind(host = host, port = port, handler = this, factory = ssl.socketFactory)
//    }

    fun attach(state: HttpConnectionState, handler: ((req: HttpRequest, resp: HttpResponse) -> Unit)? = null) {
        val con = manager.attach(state.channel)
        runProcessing(con, state, handler)
    }

    /**
     * Update network events
     *
     * @param timeout Timeout for wait event
     */
//    fun update(timeout: Int? = null) = manager.update(timeout)
}