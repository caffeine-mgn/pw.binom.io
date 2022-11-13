package pw.binom.io.httpServer

import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.create
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.URL

suspend fun get(url: URL): String {
    HttpClient.create().use {
        println("TEST-HTTP-CLIENT:Connect to $url")
        val con = it.connect(
            method = "GET",
            uri = url,
        )
        println("TEST-HTTP-CLIENT: Getting response...")
        val resp = con.getResponse()
        println("TEST-HTTP-CLIENT:Response ${resp.responseCode}")
        if (resp.responseCode != 200) {
            throw Exception("Invalid response code: ${resp.responseCode}")
        }
        println("HTTP-CLIENT:Reading response text...")
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
