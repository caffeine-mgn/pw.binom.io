package pw.binom.io.httpServer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.io.use
import pw.binom.net.toURL
import pw.binom.network.Network
import pw.binom.network.NetworkAddress
import pw.binom.network.TcpServerConnection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class HttpServerTest {

    @Test
    fun poolForWebSocketTest() = runTest(dispatchTimeoutMs = 10_000) {
        var httpServer: HttpServer? = null
        httpServer = HttpServer(
            handler = Handler { req ->
                assertTrue(req.isReadyForResponse)
                val connection = req.acceptWebsocket()
                assertFalse(req.isReadyForResponse)
                connection.read().asyncClose()
            }
        )
        httpServer.use {
            val port = TcpServerConnection.randomPort()
            it.listenHttp(
                address = NetworkAddress.Immutable(port = port)
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
    fun poolForRequestsTest() = runTest(dispatchTimeoutMs = 100_000) {
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
                }
            )
            httpServer.use {
                val port = TcpServerConnection.randomPort()
                it.listenHttp(
                    address = NetworkAddress.Immutable(port = port)
                )
                delay(1_000)
//            get("http://127.0.0.1:4444/".toURL())
                get("http://127.0.0.1:$port/".toURL())
                assertEquals(1, httpServer.httpRequest2Impl.size)
                assertEquals(1, httpServer.httpResponse2Impl.size)
            }
        }
    }
}
