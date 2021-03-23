package pw.binom.io.httpServer

import pw.binom.concurrency.Worker
import pw.binom.concurrency.sleep
import pw.binom.getOrException
import pw.binom.io.http.HTTPMethod
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.toURI
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.execute
import kotlin.test.Test
import kotlin.test.assertEquals

val okHandler = Handler {
    it.response().use {
        it.status = 202
        it.headers.contentType = "text/html;charset=utf-8"
        it.writeText().use {
            it.append("Hello! Привет в UTF-8")
        }
    }
}

class KeepAliveTest {

    @Test
    fun test() {
        val w = Worker()
        val nd = NetworkDispatcher()
        val server = HttpServer(
            manager = nd,
            handler = okHandler,
            maxIdleTime = 1_000
        )
        val addr = NetworkAddress.Immutable("127.0.0.1", 33838)
        server.bindHttp(addr)

        val d = nd.async {
            val client = HttpClient(nd)
            client.request(HTTPMethod.GET, "http://127.0.0.1:${addr.port}".toURI()).getResponse().readText()
                .use { it.readText() }
            assertEquals(1, server.idleConnectionSize)
            server.forceIdleCheck()
            assertEquals(1, server.idleConnectionSize)
            execute(w) {
                Worker.sleep(1500)
            }
            assertEquals(1, server.forceIdleCheck())
            assertEquals(0, server.idleConnectionSize)
            Unit
        }

        while (!d.isDone) {
            nd.select()
        }

        d.getOrException()
    }
}