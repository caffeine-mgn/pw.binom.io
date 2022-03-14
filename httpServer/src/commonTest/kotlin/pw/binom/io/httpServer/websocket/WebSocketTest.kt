package pw.binom.io.httpServer.websocket

import kotlinx.coroutines.test.runTest
import pw.binom.io.bufferedReader
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.toURL
import pw.binom.network.NetworkAddress
import pw.binom.network.TcpServerConnection
import pw.binom.nextUuid
import pw.binom.wrap
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebSocketTest {

    private fun handler(responseCallback: (String) -> Unit) = object : Handler {
        override suspend fun request(req: HttpRequest) {
            val con = req.acceptWebsocket()
            val input = con.read().use {
                it.bufferedReader().readText()
            }
            responseCallback(input)
        }
    }

    @Test
    fun test() = runTest {
        val message = Random.nextUuid().toString()
        val port = TcpServerConnection.randomPort()
        var ok = false
        HttpServer(handler = handler { assertEquals(message, it); ok = true }).use { httpServer ->
            httpServer.listenHttp(address = NetworkAddress.Immutable(port = port))
            HttpClient.create().use { client ->
                client.connect("GET", "ws://127.0.0.1:$port/".toURL())
                    .startWebSocket(masking = false).write(MessageType.BINARY).use {
                        message.encodeToByteArray().wrap { b -> it.write(b) }
                    }
            }
        }
        assertTrue(ok)
    }
}

// class WebSocketTest {
//
//    private class TestWebSocketHandler(val testMsg: String) : Handler {
//        override suspend fun request(req: HttpRequest) {
//            val ws = req.acceptWebsocket()
//            while (true) {
//                val text = ws.read().bufferedAsciiReader().use { it.readText() }
//                assertEquals(testMsg, text)
//                ws.write(MessageType.TEXT).bufferedAsciiWriter().use { it.append("Echo ").append(testMsg) }
//            }
//        }
//    }
//
//    @Test
//    fun serverTest() {
//        val testMsg = Random.nextUuid().toString()
//        val port = Random.nextInt(3000, Short.MAX_VALUE.toInt() - 1)
//
//        val manager = NetworkDispatcher()
//
//        val server = HttpServer(manager, TestWebSocketHandler(testMsg))
//        server.bindHttp(NetworkAddress.Immutable(host = "0.0.0.0", port = port))
//
//        val f = manager.startCoroutine {
//            val cl = BaseHttpClient(manager)
//            val con = cl.connect(HTTPMethod.GET.code, "http://127.0.0.1:$port".toURI()).startWebSocket()
//            con.write(MessageType.TEXT).bufferedAsciiWriter().use { it.append(testMsg) }
//            val text = con.read().bufferedAsciiReader().use { it.readText() }
//            assertEquals("Echo $testMsg", text)
//            con.write(MessageType.TEXT).bufferedAsciiWriter().use { it.append(testMsg) }
//            val text2 = con.read().bufferedAsciiReader().use { it.readText() }
//            assertEquals("Echo $testMsg", text2)
//            con.asyncClose()
//
//            val con2 = cl.connect(HTTPMethod.GET.code, "http://127.0.0.1:$port".toURI()).startWebSocket()
//            con2.write(MessageType.TEXT).bufferedAsciiWriter().use { it.append(testMsg) }
//            val text3 = con2.read().bufferedAsciiReader().use { it.readText() }
//            assertEquals("Echo $testMsg", text3)
//            server.asyncClose()
//        }
//        while (!f.isDone) {
//            manager.select(1000)
//        }
//        f.joinAndGetOrThrow()
//    }
// }
