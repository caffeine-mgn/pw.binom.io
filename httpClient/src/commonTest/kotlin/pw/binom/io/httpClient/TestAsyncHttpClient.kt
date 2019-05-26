package pw.binom.io.httpClient

import pw.binom.Thread
import pw.binom.URL
import pw.binom.async
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HandlerImpl(val txt: String) : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        req.headers.forEach { k ->
            k.value.forEach {
                println("${k.key}: $it")
            }
        }
        resp.addHeader("Content-Length", txt.length.toString())
        resp.output.utf8Appendable().append(txt)
        resp.status = 200

        val list = assertNotNull(req.headers["X-Server"])
        assertEquals(1, list.size)
        assertEquals("OLOLO", list[0])
    }

}

class TestAsyncHttpClient {

    @Test
    fun test() {
        val txt = "Hello from server"
        var done = false
        val server = HttpServer(HandlerImpl(txt))
        val port = 9746
        server.bind(host = "127.0.0.1", port = port)
        val clientPoll = AsyncHttpClient(server.manager)
        async {
            clientPoll.request("GET", url = URL("http://127.0.0.1:$port")).use {
                it.addRequestHeader("X-Server", "OLOLO")
                done = true
            }
        }
        val time = Thread.currentTimeMillis()
        while (!done) {
            if (Thread.currentTimeMillis() - time > 2000)
                break
            server.manager.update(1000)
        }

        clientPoll.close()
    }
}