package pw.binom.io.httpServer

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.io.socket.ServerSocketChannel
import pw.binom.io.socket.SocketClosedException
import pw.binom.io.socket.SocketFactory
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.socket.rawSocketFactory
import pw.binom.pool.DefaultPool
import pw.binom.ssl.SSLContext
import pw.binom.stackTrace

/**
 * Base Http Server
 *
 * @param handler request handler
 */
open class HttpServer(val manager: SocketNIOManager,
                      protected val handler: Handler,
                      bufferSize: Int = DEFAULT_BUFFER_SIZE,
                      poolSize: Int = 10
) : Closeable, SocketNIOManager.ConnectHandler {

    private val returnToPoolForOutput: (NoCloseOutput) -> Unit = {
        outputBufferPool.recycle(it)
    }

    private val inputBufferPool = DefaultPool(poolSize) {
        NoCloseInput()
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
        HttpResponseImpl2(httpResponseBodyPool)
    }

    private fun runProcessing(connection: SocketNIOManager.ConnectionRaw, state: HttpConnectionState?, handler: ((req: HttpRequest, resp: HttpResponse) -> Unit)?) {
        connection {
            println("New Connection!")
            while (true) {
                try {
                    val keepAlive = ConnectionProcessing.process(
                            connection = connection,
                            handler = this.handler,
                            inputBufferPool = inputBufferPool,
                            httpRequestPool = httpRequestPool,
                            httpResponsePool = httpResponsePool
                    )
                    if (!keepAlive) {
                        it.close()
                        break
                    }
                } catch (e: SocketClosedException) {
                    break
                } catch (e: Throwable) {
//                    println("Error: $e")
//                    e.stackTrace.forEach {
//                        println(it)
//                    }
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