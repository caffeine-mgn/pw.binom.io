package pw.binom.io.examples.httpServer

import pw.binom.ByteBufferPool
import pw.binom.async
import pw.binom.charset.Charsets
import pw.binom.io.*
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
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
            count++
            val client = server.accept()!!
            val reader = client.bufferedReader(pool, charBufferSize = 1024)
            try {
                time += measureTime {
                    reader.readln()
                    if (count % 500L == 0L) {
                        println("Time: ${time.inMilliseconds / count}")
                    }
                }
//                println("Request ${header[0]} ${header[1]} $count")

                //skip all request headers
                while (true) {
                    if (reader.readln()?.isNotEmpty() != false)
                        break
                }

                val static = ByteArrayOutput()
                val txt = """<html>
                |<title>Binom Example Web Server</title>
                |<body>
                |  Hello from Simple server based on <b>Binom IO</b>
                |</body>
                |</html>""".trimMargin()

                static.bufferedWriter(pool).also {
                    it.appendln("HTTP/1.1 200 OK\r\n")
                        .append("Server: Binom Example Server\r\n")
                        .append("Content-Type: text/html; charset=utf-8\r\n")
                        .append("Content-Length: ${txt.length}\r\n")
                        .append("Connection: close\r\n")
                        .append("\r\n")
                        .append("\r\n")
                        .append(txt)
                    it.flush()
                }
                static.utf8Appendable()
                static.data.flip()
                client.write(static.data)
                static.close()
            } catch (e: IOException) {
                //NOP
            } finally {
                client.close()
            }
        }
    }
    while (true) {
        nioManager.wait()
    }
}