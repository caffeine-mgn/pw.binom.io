package pw.binom.io.examples.httpClient

import pw.binom.asUTF8String
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.copyTo
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.URL

fun main(args: Array<String>) {
    val url = URL("http://example.com/")

    val connections = HttpClient()

    val con = connections.request(url)
    con.responseHeaderNames.forEach { key ->
        con.getResponseHeader(key)?.forEach {
            println("$key: $it")
        }
    }


//    val ss = InflateInputStream(con,bufferSize = 512,wrap = false)
    val bb = ByteArrayOutputStream()
    con.inputStream.copyTo(bb)
    val data = bb.toByteArray()
    println("Response Code: ${con.responseCode}")
    println("Response size: ${data.size}")
    println("Response Body: ${data.asUTF8String()}")
    con.close()
    connections.close()
}