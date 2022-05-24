package pw.binom.io.httpServer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.concurrency.WorkerPool
import pw.binom.io.use
import pw.binom.net.toURL
import pw.binom.network.Network
import pw.binom.network.NetworkAddress
import pw.binom.network.TcpServerConnection
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class HttpServerTest {

    @Test
    fun poolForWebSocketTest() = runTest {
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
    fun poolForRequestsTest() = runTest {
        var httpServer: HttpServer? = null
        httpServer = HttpServer(
            handler = Handler { req ->
                req.response {
                    it.status = 200
                    it.sendText("Hello")
                }
                assertEquals(0, httpServer?.httpRequest2Impl?.size)
                assertEquals(0, httpServer?.httpResponse2Impl?.size)
            }
        )
        httpServer.use {
            val port = TcpServerConnection.randomPort()
            it.listenHttp(
                address = NetworkAddress.Immutable(port = port)
            )
            get("http://127.0.0.1:$port/".toURL())
            assertEquals(1, httpServer.httpRequest2Impl.size)
            assertEquals(1, httpServer.httpResponse2Impl.size)
        }
    }

    @Ignore
    @Test
    fun test() = runTest {
        val oo = WorkerPool(10)
        val server = HttpServer(
            handler = Handler {
                it.response {
                    it.status = 202
                    it.headers.contentType = "text/html;charset=utf-8"
                    it.startWriteText().use {
                        it.append("Hello! Привет в UTF-8   disoather: ${coroutineContext[ContinuationInterceptor]}")
                        withContext(oo) {
                            it.append("и ${kotlin.coroutines.coroutineContext[ContinuationInterceptor]}")
                        }
                    }
                }
            }
        )
        val listenJob = server.listenHttp(address = NetworkAddress.Immutable(port = 8003))
//            var closed = false
        launch {
            delay(10_000)
            listenJob.cancel()
//                closed=true
        }
        listenJob.join()
//            while (!listenJob) {
//                delay(1_000)
//            }
    }
}
