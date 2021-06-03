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
import pw.binom.io.http.websocket.WebSocketClosedException
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
        override suspend fun request(req: HttpRequest) {
            val ws = req.acceptWebsocket()
            while (true) {
                val text = ws.read().bufferedAsciiReader().use { it.readText() }
                assertEquals(testMsg, text)
                ws.write(MessageType.TEXT).bufferedAsciiWriter().use { it.append("Echo ").append(testMsg) }
            }
        }
    }

    @Test
    fun serverTest() {
        val testMsg = Random.nextUuid().toString()
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
            con.write(MessageType.TEXT).bufferedAsciiWriter().use { it.append(testMsg) }
            val text2 = con.read().bufferedAsciiReader().use { it.readText() }
            assertEquals("Echo $testMsg", text2)
            con.asyncClose()

            val con2 = cl.request(HTTPMethod.GET, "http://127.0.0.1:$port".toURI()).startWebSocket()
            con2.write(MessageType.TEXT).bufferedAsciiWriter().use { it.append(testMsg) }
            val text3 = con2.read().bufferedAsciiReader().use { it.readText() }
            assertEquals("Echo $testMsg", text3)
            server.asyncClose()
        }
        while (!f.isDone) {
            manager.select(1000)
        }
        f.joinAndGetOrThrow()
    }
}