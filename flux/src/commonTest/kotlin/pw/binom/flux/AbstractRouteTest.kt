package pw.binom.flux

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.async
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.socket.nio.SocketNIOManager
import kotlin.test.Test
import kotlin.test.assertEquals

class AbstractRouteTest {

    class MockAction(method: String, contextUri: String) : Action {
        override val req: HttpRequest = object : HttpRequest {
            override val method: String = method
            override val uri: String = contextUri
            override val contextUri: String = contextUri
            override val input: AsyncInput
                get() = TODO("Not yet implemented")
            override val rawInput: AsyncInput
                get() = TODO("Not yet implemented")
            override val rawOutput: AsyncOutput
                get() = TODO("Not yet implemented")
            override val rawConnection: SocketNIOManager.TcpConnectionRaw
                get() = TODO("Not yet implemented")
            override val headers: Map<String, List<String>>
                get() = TODO("Not yet implemented")

        }
        override val resp: HttpResponse
            get() = TODO("Not yet implemented")

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
        var exception: Throwable? = null
        async {
            try {
                router.execute(MockAction("GET", "/events/ssdf"))
                assertEquals(1, state)

                router.execute(MockAction("GET", "/eventss/ssdf"))
                assertEquals(2, state)
            } catch (e: Throwable) {
                exception = e
            }
        }
        if (exception != null)
            throw exception!!
    }
}