package pw.binom.io.httpServer.websocket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.ByteBufferFactory
import pw.binom.io.bufferedReader
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.connectWebSocket
import pw.binom.io.httpClient.create
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServer2
import pw.binom.io.httpServer.acceptWebsocket
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.use
import pw.binom.io.useAsync
import pw.binom.io.wrap
import pw.binom.network.Network
import pw.binom.network.TcpServerConnection
import pw.binom.pool.GenericObjectPool
import pw.binom.url.URL
import pw.binom.url.toURL
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class WebSocketTest {
  private fun handler(responseCallback: (String) -> Unit) =
    Handler { req ->
      val con = req.acceptWebsocket()
      val input =
        con.read().useAsync {
          it.bufferedReader().readText()
        }
      responseCallback(input)
    }

  @Test
  fun test() =
    runTest(dispatchTimeoutMs = 10_000) {
      val message = Random.nextUuid().toString()
      val port = TcpServerConnection.randomPort()
      var ok = false
      var ok2 = false
      val handler =
        HttpHandler {
          val income = it.acceptWebsocket().useAsync { it.read().bufferedReader().useAsync { it.readText() } }
          assertEquals(message, income)
          ok = true
        }
      val handlerWrapper =
        HttpHandler { req ->
          handler.handle(req)
          ok2 = true
        }
      HttpServer2(
        handler = handlerWrapper,
        dispatcher = Dispatchers.Network,
        byteBufferPool = GenericObjectPool(ByteBufferFactory(DEFAULT_BUFFER_SIZE)),
      ).useAsync { httpServer ->
        httpServer.listen(address = InetSocketAddress.resolve(host = "127.0.0.1", port = port))
        HttpClient.create().use { client ->
          connectAndSendText(
            url = "ws://127.0.0.1:$port/".toURL(),
            text = message,
          )
        }
      }
      withContext(Dispatchers.Default) {
        delay(1.seconds)
      }
      assertTrue(ok)
      assertTrue(ok2, "Request looks like unhandled")
    }

  private suspend fun connectAndSendText(
    url: URL,
    text: String,
  ) {
    HttpClient.create().use { client ->
      client.connectWebSocket(url)
        .start().useAsync { wsConnect ->
          wsConnect.write(MessageType.BINARY).useAsync {
            text.encodeToByteArray().wrap().use { b -> it.write(b) }
          }
        }
    }
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
