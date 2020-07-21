package pw.binom.io.httpServer.websocket

import pw.binom.*
import pw.binom.io.*
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.thread.Runnable
import pw.binom.thread.Thread
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

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
                    it.utf8Appendable().append("echo: $text")
                }
            } catch (e: Throwable) {
                e.printStacktrace()
            }
        }
    }

    @Test
    fun test() {

        var done = false
        val port = Random.nextInt(3000, Short.MAX_VALUE.toInt() - 1).toShort()

        val manager = SocketNIOManager()

        val server = HttpServer(manager, TestWebSocketHandler())
        server.bindHTTP(port = port.toInt())

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

        while (!done) {
            manager.update(1000)
        }
    }
}