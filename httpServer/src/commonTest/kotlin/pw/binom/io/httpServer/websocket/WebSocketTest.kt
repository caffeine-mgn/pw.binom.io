package pw.binom.io.httpServer.websocket

import pw.binom.ByteBuffer
import pw.binom.concurrency.Worker
import pw.binom.concurrency.execute
import pw.binom.concurrency.joinAndGetOrThrow
import pw.binom.concurrency.sleep
import pw.binom.getOrException
import pw.binom.io.*
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.httpServer.HttpServer3
import pw.binom.net.toURI
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.nextUuid
import pw.binom.wrap
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketTest {

    private class TestWebSocketHandler(val testMsg: String) : Handler {
        val w = Worker()

        override suspend fun request(req: HttpRequest) {
            val ws = req.acceptWebsocket()
            val text = ws.read().bufferedAsciiReader().use { it.readText() }
            assertEquals(testMsg, text)
            ws.write(MessageType.TEXT).bufferedAsciiWriter().use { it.append("Echo ").append(testMsg) }
        }
    }

    @Test
    fun serverTest() {
        val testMsg = Random.nextUuid().toString()
        var done = false
        val port = 3000//Random.nextInt(3000, Short.MAX_VALUE.toInt() - 1).toShort()

        val manager = NetworkDispatcher()

        val server = HttpServer(manager, TestWebSocketHandler(testMsg))
        server.bindHttp(NetworkAddress.Immutable(host = "0.0.0.0", port = port))

        val f = manager.async {
            val cl = HttpClient(manager)
            val con = cl.request(HTTPMethod.GET, "http://127.0.0.1:$port".toURI()).startWebSocket()
            con.write(MessageType.TEXT).bufferedAsciiWriter().use { it.append(testMsg) }
            val text = con.read().bufferedAsciiReader().use { it.readText() }
            assertEquals("Echo $testMsg", text)
        }
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
        while (!f.isDone) {
            manager.select(1000)
        }
        f.joinAndGetOrThrow()
    }
}