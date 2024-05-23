package pw.binom.io.httpServer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.ByteBufferPool
import pw.binom.io.bufferedAsciiWriter
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.io.readBytes
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.use
import pw.binom.io.useAsync
import pw.binom.network.*
import pw.binom.testing.Testing
import pw.binom.testing.shouldContentEquals
import pw.binom.testing.shouldEquals
import pw.binom.url.toURL
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class HttpServerTest {
  private val OK_HANDLER =
    Handler { req ->
      assertTrue(req.isReadyForResponse)
      req.response {
        it.status = 200
        it.sendText("OK")
      }
    }

  suspend fun makeHttpQuery(
    networkManager: NetworkManager,
    url: String = "/",
    keepAlive: Boolean,
    port: Int,
  ): TcpConnection {
    Headers.CONNECTION
    val connection = networkManager.tcpConnect(InetSocketAddress.resolve(host = "127.0.0.1", port = port))
    connection.bufferedAsciiWriter(closeParent = false).useAsync {
      it.append("GET $url HTTP/1.0\r\n")
        .append("Host: 127.0.0.1\r\n")
        .append("Content-Length: 0\r\n")
      it.append("Connection: ")
      if (keepAlive) {
        it.append(Headers.KEEP_ALIVE)
      } else {
        it.append(Headers.CLOSE)
      }
      it.append("\r\n\r\n")
    }
    return connection
  }

  @Test
  fun baseEchoTest() = Testing.async {
    MultiFixedSizeThreadNetworkDispatcher(4).use { nd ->
      val server = HttpServer2(
        handler = HttpHandler { ex ->
          val bytes = ex.input.readBytes().decodeToString()
          println("bytes->$bytes")
          ex.response().also {
            it.status = 200
            it.send("Echo: $bytes")
          }
        },
        dispatcher = nd,
        byteBufferPool = ByteBufferPool(10)
      )
      val port = TcpServerConnection.randomPort()
      server.listen(InetSocketAddress.resolve(host = "127.0.0.1", port = port))
      val bytes = Random.nextUuid().toShortString()
      val resp = HttpClient.create().use { client ->
        client.connect(method = "POST", uri = "http://127.0.0.1:$port/".toURL()).useAsync { connect ->
          connect.writeText { it.append(bytes) }
          connect.getResponse().readAllText()
        }
      }
      resp shouldEquals "Echo: $bytes"
    }
  }

  @Test
  @Ignore
  fun testIdleTimeout() =
    runTest(timeout = 10.seconds) {
      NetworkCoroutineDispatcherImpl().use { nd ->
        withContext(nd) {
          val port = TcpServerConnection.randomPort()
          HttpServer(
            handler = OK_HANDLER,
            maxIdleTime = 2.seconds,
            manager = nd,
          ).useAsync { httpServer ->
            httpServer.listenHttp(InetSocketAddress.resolve(host = "127.0.0.1", port = port), networkManager = nd)

            makeHttpQuery(
              networkManager = nd,
              port = port,
              keepAlive = true,
            )
            delay(1.seconds)
            assertEquals(1, httpServer.idleConnectionSize)
            delay(4.seconds)
            assertEquals(0, httpServer.idleConnectionSize)
          }
        }
      }
    }

  @Test
  @Ignore
  fun poolForWebSocketTest() =
    runTest(timeout = 10.seconds) {
      var httpServer: HttpServer? = null
      httpServer =
        HttpServer(
          handler =
          Handler { req ->
            assertTrue(req.isReadyForResponse)
            val connection = req.acceptWebsocket()
            assertFalse(req.isReadyForResponse)
            connection.read().asyncClose()
          },
        )
      httpServer.useAsync {
        val port = TcpServerConnection.randomPort()
        it.listenHttp(
          address = InetSocketAddress.resolve(host = "127.0.0.1", port = port),
        )
        assertEquals(0, httpServer.httpRequest2Impl.size)
        assertEquals(0, httpServer.httpResponse2Impl.size)
        ws("http://127.0.0.1:$port/".toURL()) {
          assertEquals(0, httpServer.httpRequest2Impl.size)
          assertEquals(0, httpServer.httpResponse2Impl.size)
        }
        withContext(Dispatchers.Network) {
          delay(1.seconds)
        }
        assertEquals(1, httpServer.httpRequest2Impl.size)
        assertEquals(1, httpServer.httpResponse2Impl.size)
      }
    }

  @Test
  @Ignore
  fun poolForRequestsTest() =
    runTest(timeout = 10.seconds) {
      withContext(Dispatchers.Network) {
        var httpServer: HttpServer? = null
        httpServer =
          HttpServer(
            handler = { req ->
              println("TEST-HTTP-SERVER: Request incoming!")
              req.response {
                it.status = 200
                it.sendText("Hello")
              }
              println("TEST-HTTP-SERVER: Request processed!")
              assertEquals(0, httpServer?.httpRequest2Impl?.size)
              assertEquals(0, httpServer?.httpResponse2Impl?.size)
            },
          )
        httpServer.useAsync {
          val port = TcpServerConnection.randomPort()
          it.listenHttp(
            address = InetSocketAddress.resolve(host = "127.0.0.1", port = port),
          )
          delay(1_000)
//            get("http://127.0.0.1:4444/".toURL())
          get("http://127.0.0.1:$port/".toURL())
          get("http://127.0.0.1:$port/".toURL())
          assertEquals(1, httpServer.httpRequest2Impl.size)
          assertEquals(1, httpServer.httpResponse2Impl.size)
        }
      }
    }
}
