package pw.binom.flux

import pw.binom.*
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.net.Path
import pw.binom.net.Query
import pw.binom.net.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class AbstractRouteTest {

    class MockAction(method: String, contextUri: Path) : HttpRequest {
        override val method: String=method
        override val headers: Headers
            get() = TODO("Not yet implemented")
        override val path: Path =contextUri
        override val query: Query?
            get() = null
        override val request: String
            get() = TODO("Not yet implemented")

        override fun readBinary(): AsyncInput {
            TODO("Not yet implemented")
        }

        override fun readText(): AsyncReader {
            TODO("Not yet implemented")
        }

        override suspend fun acceptWebsocket(): WebSocketConnection {
            TODO("Not yet implemented")
        }

        override suspend fun rejectWebsocket() {
            TODO("Not yet implemented")
        }

        override suspend fun response(): HttpResponse {
            TODO("Not yet implemented")
        }

        override suspend fun <T> response(func: (HttpResponse) -> T): T {
            TODO("Not yet implemented")
        }

        override val response: HttpResponse?
            get() = null

        override suspend fun asyncClose() {
            TODO("Not yet implemented")
        }

    }

    @Test
    fun testOrder() {
        val router = RootRouter()
        var state = 0
        router.get("/*") {
            state = 2
            true
        }
        router.get("/events/*") {
            state = 1
            true
        }
        val done = async2 {
            router.execute(MockAction("GET", "/events/ssdf".toPath))
            assertEquals(1, state)

            router.execute(MockAction("GET", "/eventss/ssdf".toPath))
            assertEquals(2, state)
        }
        if (done.isFailure) {
            throw done.exceptionOrNull!!
        }
    }
}