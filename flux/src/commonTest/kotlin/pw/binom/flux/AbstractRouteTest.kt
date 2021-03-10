package pw.binom.flux

import pw.binom.*
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.HttpRequest2
import pw.binom.io.httpServer.HttpResponse2
import kotlin.test.Test
import kotlin.test.assertEquals

class AbstractRouteTest {

    class MockAction(method: String, contextUri: URN) : Action {
        override val req = object : HttpRequest2 {
            override val method: String
                get() = method
            override val headers: Headers
                get() = TODO("Not yet implemented")
            override val urn: URN
                get() = contextUri

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

            override suspend fun response(): HttpResponse2 {
                TODO("Not yet implemented")
            }

            override suspend fun asyncClose() {
                TODO("Not yet implemented")
            }

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
            router.execute(MockAction("GET", "/events/ssdf".toURN))
            assertEquals(1, state)

            router.execute(MockAction("GET", "/eventss/ssdf".toURN))
            assertEquals(2, state)
        }
        if (done.isFailure) {
            throw done.exceptionOrNull!!
        }
    }
}