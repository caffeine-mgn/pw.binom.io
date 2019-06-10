package pw.binom.io.httpClient

import pw.binom.Thread
import pw.binom.URL
import pw.binom.async
import pw.binom.io.*
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.HttpServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HandlerImpl(val txt: String, val chunked: Boolean) : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        req.headers.forEach { k ->
            k.value.forEach {
                println("${k.key}: $it")
            }
        }
        if (chunked) {
            resp.addHeader("Transfer-Encoding", "chunked")
        } else {
            resp.addHeader("Content-Length", txt.length.toString())
        }

        resp.status = 200
        resp.output.write("${txt.length.toString(16)}\r\n")
        resp.output.utf8Appendable().append(txt)
        resp.output.write("\r\n0\r\n\r\n")

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
        val server = HttpServer(HandlerImpl(txt = txt, chunked = false))
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
        server.close()
    }

    @Test
    fun chunkedTest() {
        val txt = "Hello from server"
        var done = false
        val server = HttpServer(HandlerImpl(txt = txt, chunked = true))
        val port = 9746
        server.bind(host = "127.0.0.1", port = port)
        val clientPoll = AsyncHttpClient(server.manager)
        async {
            clientPoll.request("GET", url = URL("http://127.0.0.1:$port")).use {
                it.addRequestHeader("X-Server", "OLOLO")
                val resp = it.inputStream.utf8Reader().readText()
                assertEquals(txt, resp)

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
        server.close()
    }
}