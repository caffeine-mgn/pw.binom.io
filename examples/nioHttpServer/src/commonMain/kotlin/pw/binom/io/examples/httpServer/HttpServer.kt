package pw.binom.io.examples.httpServer

import pw.binom.ByteBufferPool
import pw.binom.async
import pw.binom.io.*
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.seconds

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val nioManager = NetworkDispatcher()
    val server = nioManager.bindTcp(NetworkAddress.Immutable(port = 8080))
    val pool = ByteBufferPool(10)
    var time = 0.seconds
    var count = 0L
    async {
        while (true) {
            val t = measureTime {
                val client = server.accept()!!
                val reader = client.bufferedReader(pool)
                try {
                    val header = reader.readln()!!.split(' ')
                    if (header[1] == "/t") {
                        println("Time: ${time.inMilliseconds / count}")
                    }
//                println("Request ${header[0]} ${header[1]}")

                    //skip all request headers
                    while (true) {
                        if (reader.readln()?.isNotEmpty() != false)
                            break
                    }

                    val txt = """<html>
                |<title>Binom Example Web Server</title>
                |<body>
                |  Hello from Simple server based on <b>Binom IO</b>
                |</body>
                |</html>""".trimMargin()
                    val app = client.bufferedOutput().also {
                        val app = it.utf8Appendable()
                        app.appendln("HTTP/1.1 200 OK")
                            .appendln("Server: Binom Example Server")
                            .appendln("Content-Type: text/html; charset=utf-8")
                            .appendln("Content-Length: ${txt.length}")
                            .appendln("Connection: close")
                            .appendln("")
                            .appendln("")
                            .appendln(txt)
                        it.flush()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    //NOP
                } finally {
                    client.close()
                }
            }
            time += t
            count++
        }
    }
    while (true) {
        nioManager.wait()
    }
}