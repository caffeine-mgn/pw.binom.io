package pw.binom.io.httpClient

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.io.http.HTTPMethod
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.io.use
import pw.binom.network.*
import pw.binom.url.toURL
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime

@ExperimentalTime
class TestAsyncBaseHttpClient {

    @Test
    fun timeoutTest2() = runTest(dispatchTimeoutMs = 10_000) {
        Dispatchers.Network.bindTcp(InetNetworkAddress.create(host = "127.0.0.1", port = 0))
            .use { server ->
                val serverPort = server.port
                println("server binded port: ${server.port}")
                launch {
                    println("wait client")
                    try {
                        server.accept()
                        println("client connected")
                    } catch (e: Throwable) {
                        // Do nothing
                    }
                }
                HttpClient.create().use { client ->
                    try {
                        realWithTimeout(5.seconds) {
                            client.connect(
                                method = "GET",
                                uri = "http://127.0.0.1:$serverPort/".toURL(),
//                                        uri = "http://example.com/".toURI(),
                            ).getResponse().readText().use { it.readText() }
                        }
//                                }
                    } catch (e: CancellationException) {
                        // Do nothing
                    }
                }
            }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun timeoutTest() = runTest(dispatchTimeoutMs = 10_000) {
        val manager = NetworkCoroutineDispatcherImpl()
        val client = HttpClient.create(networkDispatcher = Dispatchers.Network)
        val server = manager.bindTcp(InetNetworkAddress.create(host = "127.0.0.1", port = TcpServerConnection.randomPort()))
        val now = TimeSource.Monotonic.markNow()
        try {
            realWithTimeout(3.seconds) {
                client.connect(HTTPMethod.GET, "http://127.0.0.1:${server.port}".toURL()).getResponse().responseCode
            }
            fail()
        } catch (e: CancellationException) {
            val time = now.elapsedNow()
            println("Real timeout: $time")
            assertTrue(time >= 3.seconds && time < 4.seconds)
        }
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun test() = runTest(dispatchTimeoutMs = 10_000) {
        withContext(Dispatchers.Network) {
            val client = HttpClient.create(
                networkDispatcher = Dispatchers.Network,
                useKeepAlive = false,
//            connectTimeout = Duration.INFINITE,
            )
            repeat(3) {
                val testTime = measureTime {
                    val responseData =
                        client.connect(HTTPMethod.GET, "https://google.com/".toURL())
                            .getResponse()
                            .also {
                                println("headers:${it.inputHeaders}")
                            }.readData().use {
                                it.skipAll()
                            }
                }
                println("testTime=$testTime")
            }
        }
    }
}

/*
class HandlerImpl(val txt: String, val chunked: Boolean) : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        req.headers.forEach { k ->
            k.value.forEach {
                println("${k.key}: $it")
            }
        }
        if (chunked) {
            resp.addHeader(Headers.TRANSFER_ENCODING, Headers.CHUNKED)
        } else {
            resp.addHeader(Headers.CONTENT_LENGTH, txt.length.toString())
        }

        resp.status = 200
        val out = resp.complete()
        out.utf8Appendable().append(txt)
        out.flush()

        val list = assertNotNull(req.headers["X-Server"])
        assertEquals(1, list.size)
        assertEquals("OLOLO", list[0])
    }
}

class EmptyHandler : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        resp.addHeader(Headers.CONTENT_LENGTH, "0")
        val txt = req.input.utf8Reader().readText()
        resp.status = 204
        println("txt=$txt")
//        val gg = req.input.utf8Reader().readText()
//        println("EmptyHandler::message::$gg")
    }

}

class TestAsyncHttpClient {

/*    @Test
    fun test() {
        val manager = SocketNIOManager()
        val client = AsyncHttpClient(manager)
        async {
            client.request("GET", URL("http://api.tlsys.org/client")).use {
                it.addRequestHeader(Headers.CONTENT_TYPE, "application/json; charset=utf-8")
                it.outputStream.utf8Appendable().append("""{"organization":null,"businessUnit":null,"workPlace":"fdf896f5-e622-4c84-b5b9-8fc19b66834a","client":{"card":"112","mobilePhone":null,"email":null,"validationCode":null,"availableAmount":null}}""")
                it.outputStream.flush()
                println("Response code: ${it.responseCode()}")
                val reader = it.inputStream.utf8Reader()
                var i = 0
                while (true) {
                    val c = reader.read()?:break
                    print(c)
                    i++
                    if (i>1000)
                        break
                }
                println("Break")
            }
        }
        while (true) {
            manager.update()
        }
    }*/


    @Ignore
    @Test
    fun ttt() {
        val manager = SocketNIOManager()
        val client = AsyncHttpClient(manager)

        var done = false
        async {
            try {
                val out = ByteArrayOutputStream()
                val r = client.request("GET", URL("https://www.google.com/"))
                r.inputStream.copyTo(out)
                r.getResponseHeaders().forEach {
                    println("${it.key}: ${it.value}")
                }
                println("Read ${out.size}")
            } catch (e: Throwable) {
                println("Error: $e")
                e.stackTrace.forEach {
                    println(it)
                }
            } finally {
                done = true
            }
        }

        while (!done) {
            manager.update()
        }
    }

    @Test
    fun test() {
        println("---------------test---------------")
        val txt = "Hello from server"
        var done = false
        val manager = SocketNIOManager()
        val server = HttpServer(manager, HandlerImpl(txt = txt, chunked = false))
        val port = 9747
        server.bindHTTP(host = "127.0.0.1", port = port)
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
            manager.update(1000)
        }

        clientPoll.close()
        server.close()
    }

    @Test
    fun chunkedTest() {
        println("---------------chunkedTest---------------")
        val txt = "Hello from server"
        var done = false
        val manager = SocketNIOManager()
        val server = HttpServer(manager, HandlerImpl(txt = txt, chunked = true))
        val port = 9748
        server.bindHTTP(host = "127.0.0.1", port = port)
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

    @Test
    fun emptyHandler() {
        println("---------------emptyHandler---------------")
        val manager = SocketNIOManager()
        val server = HttpServer(manager, EmptyHandler())
        val clientPoll = AsyncHttpClient(server.manager)
        var done = false
        val port = 9746
        server.bindHTTP(host = "127.0.0.1", port = port)
        async {
            clientPoll.request("GET", URL("http://127.0.0.1:$port")).use {
                it.addRequestHeader(Headers.CONTENT_LENGTH, "2")
                it.outputStream.write("OK")
                it.outputStream.flush()
                assertEquals(204, it.responseCode())
            }

            println("//---------------------------------------------//")

            clientPoll.request("GET", URL("http://127.0.0.1:$port")).use {
//                it.addRequestHeader(Headers.CONTENT_LENGTH, "2")
                it.outputStream.write("OK")
                it.outputStream.flush()
                assertEquals(204, it.responseCode())
            }
            done = true
        }

        val time = Thread.currentTimeMillis()
        while (!done) {
            if (Thread.currentTimeMillis() - time > 2000)
                break
            server.manager.update(1000)
        }
        assertTrue(done)

        clientPoll.close()
        server.close()
    }
}

class DDD {

    @Test
    fun test() {
        val vv = SocketNIOManager()
        val client = AsyncHttpClient(vv)
        var done = AtomicBoolean(false)
        async {
            try {
                client.request("GET", URL("https://tlsys.ru/jre/jre-windows-32.tar"))
                        .use { r ->
                            println("Response: ${r.responseCode()}")
                            val data = ByteArray(5)
                            r.inputStream.readFully(data)
                            data.forEachIndexed { index, byte ->
                                println("Data $index -> $byte")
                            }
                        }
                done.value = true
            } catch (e: Throwable) {
                println("ERRROR: $e")
                e.stackTrace.forEach {
                    println(it)
                }
            } finally {
                done.value = true
            }
            println("Finish!")
        }

        val start = Date.now
        while (true) {
            val now = Date.now
            if (now > start + 10_000)
                break
            if (done.value)
                break
            vv.update(1000)
        }
        println("Done: $done")
    }

}
*/
