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

    protected val manager = object : ConnectionManager() {
        override fun connected(connection: Connection) {
            connection {
                try {
                    while (true) {
                        val request = it.input.readln()
                        val items = request.split(' ')
                        val headers = HashMap<String, ArrayList<String>>()

                        while (true) {
                            val s = it.input.readln()
                            if (s.isEmpty())
                                break
                            val items1 = s.split(": ", limit = 2)

                            headers.getOrPut(items1[0]) { ArrayList() }.add(items1[1])
                        }

                        val request1 = HttpRequestImpl(
                                connection = it,
                                method = items[0],
                                uri = items[1],
                                headers = headers)

                        val response = HttpResponseImpl(it)

                        handler.request(request1, response)
                        response.endResponse()

                        connection.output.writeln("")
                        connection.output.writeln("")

                        if (response.disconnectFlag) {
                            break
                        }

                        if (response.detachFlag) {
                            return@connection
                        }

                        if (response.headers["Connection"]?.singleOrNull() == "keep-alive"
                                && response.headers["Content-Length"]?.singleOrNull()?.toLongOrNull() != null) {
                            continue
                        }
                        break
                    }
                    it.close()
                } catch (e: IOException) {
                    //NOP
                    println("Disconnected ${e}")
                }
            }
        }
    }

    /**
     * Bind server to port [port]
     *
     * @param port Port for bind
     */
    fun bind(port: Int) {
        manager.bind(port)
    }

    /**
     * Update network events
     *
     * @param timeout Timeout for wait event
     */
    fun update(timeout: Int? = null) {
        manager.update(timeout)
    }
}