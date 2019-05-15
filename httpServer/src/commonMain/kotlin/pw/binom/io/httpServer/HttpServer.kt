package pw.binom.io.httpServer

import pw.binom.io.IOException
import pw.binom.io.readln
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.writeln

/**
 * Base Http Server
 *
 * @param handler request handler
 */
open class HttpServer(protected val handler: Handler) {

    private fun runProcessing(connection: ConnectionManager.Connection, state: HttpConnectionState?) {
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
                            request = request1)

                    handler.request(request1, response)


                    if (response.disconnectFlag) {
                        break
                    }

                    if (response.detachFlag) {
                        return@connection
                    }

                    response.endResponse()

                    connection.output.writeln("")
                    connection.output.writeln("")

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

    protected val manager = object : ConnectionManager() {
        override fun connected(connection: Connection) {
            runProcessing(connection, null)
        }
    }

    /**
     * Bind server to port [port]
     *
     * @param port Port for bind
     */
    fun bind(host: String = "0.0.0.0", port: Int) {
        manager.bind(host = host, port = port)
    }

    fun attach(state: HttpConnectionState) {
        val con = manager.attach(state.channel)
        runProcessing(con, state)
    }

    /**
     * Update network events
     *
     * @param timeout Timeout for wait event
     */
    fun update(timeout: Int? = null) = manager.update(timeout)
}