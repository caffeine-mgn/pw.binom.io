package pw.binom.io.httpServer

import pw.binom.ByteDataBuffer
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.*
import pw.binom.io.http.Headers
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.toDuration

suspend inline fun AsyncAppendable.appendln(text: String) = append("$text\r\n")
suspend inline fun AsyncAppendable.appendln() = append("\r\n")
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
