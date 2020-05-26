package pw.binom.io.examples.httpClient

import pw.binom.URL
import pw.binom.asUTF8String
import pw.binom.async
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.copyTo
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.nio.SocketNIOManager

fun main() {
    val url = URL("http://example.com/")

    val nioManager = SocketNIOManager()
    val connections = AsyncHttpClient(nioManager)

    val con = connections.request("GET", url)
    val done = AtomicBoolean(false)
    async {
        try {
            con.getResponseHeaders().forEach { key ->
                key.value.forEach {
                    println("${key.key}: $it")
                }
            }

            val bb = ByteArrayOutputStream()
            con.inputStream.copyTo(bb)

            val data = bb.toByteArray()
            println("Response Code: ${con.responseCode()}")
            println("Response size: ${data.size}")
            println("Response Body: ${data.asUTF8String()}")
            con.close()
            connections.close()
        } finally {
            done.value = true
        }
    }

    while (!done.value) {
        nioManager.update()
    }
}