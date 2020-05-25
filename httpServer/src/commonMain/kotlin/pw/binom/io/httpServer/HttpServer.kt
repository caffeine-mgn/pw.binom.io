package pw.binom.io.httpServer

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.io.StreamClosedException
import pw.binom.io.readln
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.socket.ServerSocketChannel
import pw.binom.io.socket.SocketFactory
import pw.binom.io.socket.rawSocketFactory
import pw.binom.ssl.SSLContext

/**
 * Base Http Server
 *
 * @param handler request handler
 */
open class HttpServer(val manager: ConnectionManager, protected val handler: Handler) : Closeable, ConnectionManager.ConnectHandler {

    private fun runProcessing(connection: ConnectionManager.ConnectionRaw, state: HttpConnectionState?, handler: ((req: HttpRequest, resp: HttpResponse) -> Unit)?) {
        connection {
            try {
                while (true) {
                    val uri: String
                    val method: String
                    val headers = HashMap<String, ArrayList<String>>()
                    if (state == null) {
                        val request = it.input.readln()
                        val items = request.split(' ')
                        method = items[0]
                        uri = items.getOrNull(1) ?: ""
                        while (true) {
                            val s = it.input.readln()
                            if (s.isEmpty())
                                break
                            val items1 = s.split(": ", limit = 2)

                            headers.getOrPut(items1[0]) { ArrayList() }.add(items1.getOrNull(1) ?: "")
                        }
                    } else {
                        uri = state.uri
                        method = state.method
                        state.requestHeaders.forEach {
                            headers[it.key] = ArrayList(it.value)
                        }
                    }
                    val request1 = HttpRequestImpl(
                            connection = it,
                            method = method,
                            uri = uri,
                            headers = headers)

                    val response = HttpResponseImpl(
                            status = state?.status ?: 404,
                            headerSendded = state?.headerSendded ?: false,
                            headers = state?.responseHeaders ?: mapOf(),
                            connection = it,
                            request = request1,
                            keepAlive = request1.headers["Connection"]?.getOrNull(0) == "keep-alive")

                    if (handler != null) {
                        handler(request1, response)
                    } else
                        this.handler.request(request1, response)
                    try {
                        val b = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            if (request1.input.read(b) == 0)
                                break
                        }
                    } catch (e: StreamClosedException) {
                        //NOP
                    }
                    response.output.close()


                    if (response.disconnectFlag) {
                        break
                    }

                    if (response.detachFlag) {
                        return@connection
                    }

                    response.endResponse()

//                    connection.output.writeln()
//                    connection.output.writeln()

                    if (response.headers["Connection"]?.singleOrNull() == "keep-alive"
                            && response.headers["Content-Length"]?.singleOrNull()?.toLongOrNull() != null) {
                        continue
                    }
                    break
                }
                it.close()
            } catch (e: IOException) {
                it.close()
            }
        }
    }

    override fun clientConnected(connection: ConnectionManager.ConnectionRaw, manager: ConnectionManager) {
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

    fun bindHTTPS(ssl: SSLContext, host: String = "0.0.0.0", port: Int) {
        binded += manager.bind(host = host, port = port, handler = this, factory = ssl.socketFactory)
    }

    fun attach(state: HttpConnectionState, handler: ((req: HttpRequest, resp: HttpResponse) -> Unit)? = null) {
        val con = manager.attach(state.channel)
        runProcessing(con, state, handler)
    }

    /**
     * Update network events
     *
     * @param timeout Timeout for wait event
     */
    fun update(timeout: Int? = null) = manager.update(timeout)
}