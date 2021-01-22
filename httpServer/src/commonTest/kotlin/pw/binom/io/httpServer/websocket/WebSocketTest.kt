package pw.binom.io.httpServer.websocket

import pw.binom.ByteBuffer
import pw.binom.concurrency.Worker
import pw.binom.concurrency.sleep
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.wrap
import kotlin.test.Ignore
import kotlin.test.Test

class WebSocketTest {

    private class TestWebSocketHandler : WebSocketHandler() {
        val w = Worker()
        override suspend fun connected(request: ConnectRequest) {
            try {
                println("New Connection")
                val connection = request.accept()
                println("Try read message from client")
                w.execute(connection) {
                    Worker.sleep(1000)
                    println("##1")
                    it.write(MessageType.TEXT) {
                        println("##2")
                        it.write(ByteBuffer.wrap("Hello".encodeToByteArray()))
//                        it.utf8Appendable().use { it.append("Hello!") }
                    }
                }
                while (true) {
                    val msg = connection.read()
                    val text = msg.use {
                        it.utf8Reader().readText()
                    }
                    println("Message from client read!")

                    connection.write(msg.type) {
                        it.utf8Appendable().append("Echo: $text")
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    @Ignore
    @Test
    fun serverTest() {

        var done = false
        val port = 3000//Random.nextInt(3000, Short.MAX_VALUE.toInt() - 1).toShort()

        val manager = NetworkDispatcher()

        val server = HttpServer(manager, TestWebSocketHandler())
        server.bindHTTP(NetworkAddress.Immutable(host = "0.0.0.0", port = port))
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