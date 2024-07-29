package pw.binom.io.httpServer

import kotlinx.coroutines.withContext
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpClient.HttpClient
import pw.binom.io.httpClient.connectWebSocket
import pw.binom.io.httpClient.create
import pw.binom.io.use
import pw.binom.io.useAsync
import pw.binom.network.MultiFixedSizeThreadNetworkDispatcher
import pw.binom.network.NetworkManager
import pw.binom.url.URL

suspend fun get(url: URL): String {
  HttpClient.create().use {
    println("TEST-HTTP-CLIENT:Connect to $url")
    val con =
      it.connect(
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
    return resp.readText().useAsync { it.readText() }
  }
}

suspend inline fun <T> ws(
  url: URL,
  func: (WebSocketConnection) -> T,
): T =
  HttpClient.create().use {
    val resp =
      it.connectWebSocket(
        method = "GET",
        uri = url,
      )
    resp.start().useAsync(func)
  }

suspend fun network(func:suspend (NetworkManager)->Unit){
  MultiFixedSizeThreadNetworkDispatcher(4).use { nd ->
    withContext(nd){
      func(nd)
    }
  }
}
