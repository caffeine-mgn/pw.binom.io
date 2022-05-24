package pw.binom.io.httpServer

import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.URL

suspend fun get(url: URL): String {
    HttpClient.create().use {
        val con = it.connect(
            method = "GET",
            uri = url,
        )
        val resp = con.getResponse()
        if (resp.responseCode != 200) {
            throw Exception("Invalid response code: ${resp.responseCode}")
        }
        return resp.readText().use { it.readText() }
    }
}

suspend inline fun <T> ws(url: URL, func: (WebSocketConnection) -> T): T = HttpClient.create().use {
    val resp = it.connect(
        method = "GET",
        uri = url,
    )
    resp.startWebSocket().use(func)
}
