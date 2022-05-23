package pw.binom.flux

import kotlinx.coroutines.test.runTest
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.io.http.MutableHeaders
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.net.Path
import pw.binom.net.Query
import pw.binom.net.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class AbstractRouteTest {

    class MockAction(override val method: String, contextUri: Path) : HttpRequest {
        override val headers: Headers
            get() = TODO("Not yet implemented")
        override val path: Path = contextUri
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

        override suspend fun acceptWebsocket(masking: Boolean): WebSocketConnection {
            TODO("Not yet implemented")
        }

        override suspend fun rejectWebsocket() {
            TODO("Not yet implemented")
        }

        override suspend fun acceptTcp(): AsyncChannel {
            TODO("Not yet implemented")
        }

        override suspend fun rejectTcp() {
            TODO("Not yet implemented")
        }

        override suspend fun response(): HttpResponse {
            val r = HttpResponseMock()
            response = r
            return r
        }

        override var response: HttpResponse? = null
            private set
        override val isReadyForResponse: Boolean
            get() = TODO("Not yet implemented")

        override suspend fun asyncClose() {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun testOrder() = runTest {
        val router = RootRouter()
        var state = 0
        router.get("/events/*") {
            state = 1
            it.response()
        }
        router.get("/*") {
            state = 2
        }
        router.execute(MockAction("GET", "/events/ssdf".toPath))
        assertEquals(1, state)

        router.execute(MockAction("GET", "/eventss/ssdf".toPath))
        assertEquals(2, state)
    }
}

class HttpResponseMock : HttpResponse {
    override var status: Int = 404
    override val headers: MutableHeaders
        get() = TODO("Not yet implemented")

    override suspend fun startWriteBinary(): AsyncOutput {
        TODO("Not yet implemented")
    }

    override suspend fun startWriteText(): AsyncWriter {
        TODO("Not yet implemented")
    }

    override suspend fun asyncClose() {
        TODO("Not yet implemented")
    }
}
