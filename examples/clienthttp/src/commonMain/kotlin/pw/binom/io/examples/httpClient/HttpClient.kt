package pw.binom.io.examples.httpClient

import pw.binom.ByteBufferPool
import pw.binom.async2
import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.forEachHeader
import pw.binom.io.httpClient.BaseHttpClient
import pw.binom.io.use
import pw.binom.net.toURI
import pw.binom.network.NetworkDispatcher

fun main() {
    val url = "http://example.com/".toURI()

    val nioManager = NetworkDispatcher()
    val connections = BaseHttpClient(nioManager)
    val byteBufferPool = ByteBufferPool(100)

    val done = async2 {
        val tempBuffer = ByteArrayOutput()
        try {
            val con = connections.connect(HTTPMethod.GET.code, url)
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