package pw.binom.io.httpServer

import pw.binom.async2
import pw.binom.io.http.Encoding
import pw.binom.io.http.HTTPMethod
import pw.binom.io.httpClient.BaseHttpClient
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.toURI
import pw.binom.network.NetworkAddress
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

//class IdentityServerTest {
//    @Test
//    fun test() {
//        val manager = NetworkDispatcher()
//        val port = Random.nextInt(1000, Short.MAX_VALUE - 1)
//        val done = manager.startCoroutine {
//            val server = HttpServer(
//                manager = manager,
//                handler = Handler {
//                    it.response().use {
//                        it.status = 202
//                        it.headers.contentType = "text/html;charset=utf-8"
//                        it.headers.contentEncoding = "gzip"
//                        it.headers.transferEncoding = Encoding.CHUNKED
//                        it.startWriteText().use {
//                            it.append("Hello! Привет в UTF-8")
//                        }
//                    }
//                }
//            )
//            server.bindHttp(NetworkAddress.Immutable("127.0.0.1", port))
//
//            val client = BaseHttpClient(manager)
//            val resp = client.connect(HTTPMethod.GET.code, "http://127.0.0.1:$port/".toURI())
//                .getResponse()
//                .readText().use {
//                    it.readText()
//                }
//            assertEquals("Hello! Привет в UTF-8", resp)
//        }
//        while (!done.isDone) {
//            manager.select(1000)
//            if (done.isDone && done.isFailure) {
//                throw done.exceptionOrNull!!
//            }
//        }
//        if (done.isFailure) {
//            throw done.exceptionOrNull!!
//        }
//    }
//}

/*

@Ignore
@OptIn(ExperimentalTime::class)
class IdentityServerTest {

    val port = Random.nextInt(1000, 0xFFFF)
    val done = AtomicBoolean(false)
    val good = AtomicBoolean(false)

    val client = Thread(Runnable {
        val manager = SocketNIOManager()
        println("Try to connect!")
        manager.connect("127.0.0.1", port).invoke {
            println("Send Request Data")
            val app = it.utf8Appendable()
            app.appendln("GET / HTTP/1.1")
                    .appendln("Host: google.com")
                    .appendln()
            println("Header was sent!")
            val reader = it.utf8Reader()
            val status = reader.readln()
            val header = ArrayList<String>()
            while (true) {
                val s = reader.readln()
                if (s == null || s.isEmpty())
                    break
                header += s
            }

            println("Headers readed")
            val length = header.find { it.startsWith(Headers.CONTENT_LENGTH) }?.let { it.removePrefix(Headers.CONTENT_LENGTH).removePrefix(": ") }!!.toInt()
            assertEquals(out.size, length)
            println("Length: $length")
            val data = ByteDataBuffer.alloc(length)
            it.read(data)
            out.data.forEachIndexed { index, byte ->
                assertEquals(byte, data[index])
            }
            done.value = true
            good.value = true
        }

        while (!done.value) {
            manager.update(1000)
        }
    })

    val out = ByteArrayOutput()
            .also {
                it.utf8Appendable().append("Hello from serevr")
                it.trimToSize()
            }

    val totalRunTimer = Thread(Runnable {
        val startTime = TimeSource.Monotonic.markNow()
        val max = 150.0.toDuration(DurationUnit.SECONDS)
        while (true) {
            val v = startTime.elapsedNow()
            println("time: $v")
            if (v > max) {
                break
            }
            Thread.sleep(1000)
        }
        if (!done.value) {
            println("Stop Test")
            done.value = true
        }
    })

    @Test
    fun test() {
        val handler = object : Handler {
            override suspend fun request(req: HttpRequest, resp: HttpResponse) {
                println("Client conntected!")
                resp.status = 200
                resp.resetHeader(Headers.CONTENT_LENGTH, out.data.size.toString())
                resp.complete().write(out.data)
                println("Response send")
            }

        }
        val manager = SingleThreadNioManager(TODO(), TODO())
        val server = HttpServer(manager, handler)
        server.bindHTTP("127.0.0.1", port)
        println("Open server on $port")
        client.start()
        totalRunTimer.start()
        while (!done.value) {
            manager.update(1000)
        }

        if (!good.value)
            throw fail("Response Timeout")
    }
}*/
