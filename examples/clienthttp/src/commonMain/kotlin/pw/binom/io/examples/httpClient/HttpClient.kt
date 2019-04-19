package pw.binom.io.examples.httpClient

import pw.binom.asUTF8String
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.copyTo
import pw.binom.io.httpClient.HttpConnections
import pw.binom.io.httpClient.URL
import pw.binom.io.socket.Socket

fun main(args: Array<String>) {
    Socket.startup()
    val url = URL("http://example.com/")

    val connections = HttpConnections()

    val con = connections.request(url)
    con.responseHeaderNames.forEach { key ->
        con.getResponseHeader(key)?.forEach {
            println("$key: $it")
        }
    }


    val bb = ByteArrayOutputStream()
    con.copyTo(bb)
    val data = bb.toByteArray()
    println("Response Code: ${con.responseCode}")
    println("Response size: ${data.size}")
    println("Response Body: ${data.asUTF8String()}")
    con.close()
    connections.close()

    Socket.shutdown()
//    ddd()

//    val con = Connection("127.0.0.1", 8500).request("GET", "/")
//
//    con.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
//    con.addHeader("Host", "127.0.0.1")
//
//    println("Response Code: ${con.responseCode}")
//    println("Content-Type: ${con.contentType}")

//    println("Response: ${b.toByteArray().asUTF8String()}")
}