package pw.binom.io.httpServer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.io.bufferedAsciiWriter
import pw.binom.io.http.Headers
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.io.use
import pw.binom.network.*
import pw.binom.url.toURL
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class HttpServerTest {

    private val OK_HANDLER = Handler { req ->
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
        val connection = networkManager.tcpConnect(InetNetworkAddress.create(host = "127.0.0.1", port = port))
        connection.bufferedAsciiWriter(closeParent = false).use {
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
    @Ignore
    fun testIdleTimeout() = runTest(dispatchTimeoutMs = 10_000) {
        NetworkCoroutineDispatcherImpl().use { nd ->
            withContext(nd) {
                val port = TcpServerConnection.randomPort()
                HttpServer(
                    handler = OK_HANDLER,
                    maxIdleTime = 2.seconds,
                    manager = nd,
                ).use { httpServer ->
                    httpServer.listenHttp(InetNetworkAddress.create(host = "127.0.0.1", port = port), networkManager = nd)

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
    fun poolForWebSocketTest() = runTest(dispatchTimeoutMs = 10_000) {
        var httpServer: HttpServer? = null
        httpServer = HttpServer(
            handler = Handler { req ->
                assertTrue(req.isReadyForResponse)
                val connection = req.acceptWebsocket()
                assertFalse(req.isReadyForResponse)
                connection.read().asyncClose()
            },
        )
        httpServer.use {
            val port = TcpServerConnection.randomPort()
            it.listenHttp(
                address = InetNetworkAddress.create(host = "127.0.0.1", port = port),
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
    fun poolForRequestsTest() = runTest(dispatchTimeoutMs = 10_000) {
        withContext(Dispatchers.Network) {
            var httpServer: HttpServer? = null
            httpServer = HttpServer(
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
            httpServer.use {
                val port = TcpServerConnection.randomPort()
                it.listenHttp(
                    address = InetNetworkAddress.create(host = "127.0.0.1", port = port),
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
