package pw.binom.io.httpServer.websocket

import pw.binom.io.httpServer.HttpServer
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.test.Test

class WebSocketTest {

    private class TestWebSocketHandler : WebSocketHandler() {
        override suspend fun connected(request: ConnectRequest) {
            try {
                println("New Connection")
                val connection = request.accept()
                println("Try read message from client")
                val msg = connection.read()
                val text = msg.use {
                    it.utf8Reader().readText()
                }
                println("Message from client read!")

                connection.write(msg.type).use {
                    it.utf8Appendable().append(text)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    @Test
    fun test() {

        var done = false
        val port = 3000//Random.nextInt(3000, Short.MAX_VALUE.toInt() - 1).toShort()

        val manager = NetworkDispatcher()

        val server = HttpServer(manager, TestWebSocketHandler())
        server.bindHTTP(NetworkAddress.Immutable(host = "0.0.0.0", port = port.toInt()))
/*
        val str = Random.uuid().toString()
        async {
            AsyncHttpClient(manager).use { client ->
                val ws = client.request("GET", URL("ws://127.0.0.1:$port"))
                    .websocket()
                ws.write(MessageType.TEXT).utf8Appendable().use {
                    it.append(str)
                    it.flush()
                }
                ws.read().use {
                    assertEquals("echo: $str", it.utf8Reader().readText())
                    done = true
                }
            }
        }
*/
        while (!done) {
            manager.select(1000)
        }
    }
}