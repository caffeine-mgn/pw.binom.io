package pw.binom.io.examples.httpServer

import pw.binom.ByteBufferPool
import pw.binom.async
import pw.binom.charset.Charsets
import pw.binom.io.*
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val nioManager = NetworkDispatcher()
    val server = nioManager.bindTcp(NetworkAddress.Immutable(port = 7328))
    val pool = ByteBufferPool(10)
    var time = 0.seconds
    var count = 0L
    async {
        val c = Charsets.UTF8
        while (true) {
            count++
            println("accepting")
            val client = server.accept()!!
            println("accepted")
            async {
                println("Connected!")
                val reader = client.bufferedReader(pool, charBufferSize = 1024)
                val writer = client.bufferedWriter(pool, charBufferSize = 1024)
//                client.write(ByteBuffer.wrap("1-Hello!\r\n".encodeToByteArray()))
//                writer.append("2-Hello!\r\n")
//                println("Flushing...")
//                writer.flush()
//                println("Flushed!")
//                client.close()
//                return@async
                while (true) {
                    try {
                        val input = reader.readln()?.trim() ?: break
                        val output = input.reversed()
                        writer.append(output).append("\r\n")
//                        writer.appendln("GET /")
                        writer.flush()
                    } catch (e: IOException) {
                        client.close()
                        break
                    }
                }
            }
        }
    }
    while (true) {
        nioManager.select()
    }
}