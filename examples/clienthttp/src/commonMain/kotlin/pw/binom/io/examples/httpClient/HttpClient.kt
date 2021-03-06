package pw.binom.io.examples.httpClient

import pw.binom.ByteBufferPool
import pw.binom.async2
import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.forEachHeader
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.use
import pw.binom.network.NetworkDispatcher
import pw.binom.toURLOrNull

fun main() {
    val url = "http://example.com/".toURLOrNull()!!

    val nioManager = NetworkDispatcher()
    val connections = HttpClient(nioManager)
    val byteBufferPool = ByteBufferPool(100)

    val done = async2 {
        val tempBuffer = ByteArrayOutput()
        try {
            val con = connections.request(HTTPMethod.GET, url)
            val req = con.getResponse()
            println("Headers:")
            req.headers.forEachHeader { key, value ->
                println("$key: $value")
            }
            req.readData().use { it.copyTo(tempBuffer, byteBufferPool) }

            tempBuffer.trimToSize()
            tempBuffer.data.clear()
            val data = tempBuffer.data
            con.asyncClose()
            connections.close()
        } finally {
            tempBuffer.close()
        }
    }

    while (!done.isDone) {
        nioManager.select()
    }
    if (done.isFailure) {
        throw done.exceptionOrNull!!
    }
}