package pw.binom.io.examples.httpClient

import pw.binom.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteArrayOutput
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.network.NetworkDispatcher

fun main() {
    val url = URL("http://example.com/")

    val nioManager = NetworkDispatcher()
    val connections = AsyncHttpClient(nioManager)
    val byteBufferPool = ByteBufferPool(100)

    val done = AtomicBoolean(false)
    async {
        val tempBuffer = ByteArrayOutput()
        try {
            val con = connections.request("GET", url)
            val req = con.response()
            req.headers.forEach { key ->
                key.value.forEach {
                    println("${key.key}: $it")
                }
            }

            req.copyTo(tempBuffer, byteBufferPool)

            tempBuffer.trimToSize()
            tempBuffer.data.clear()
            val data = tempBuffer.data
            println("Response Code: ${req.responseCode}")
            println("Response size: ${data.capacity}")
            println("Response Body: ${data.asUTF8String()}")
            con.asyncClose()
            connections.close()
        } finally {
            tempBuffer.close()
            done.value = true
        }
    }

    while (!done.value) {
        nioManager.select()
    }
}