package pw.binom.io.httpClient

import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.readBytes
import pw.binom.io.use
import pw.binom.io.useAsync
import pw.binom.io.wrap
import pw.binom.network.MultiFixedSizeThreadNetworkDispatcher
import pw.binom.testing.shouldContentEquals
import pw.binom.testing.shouldEquals
import kotlin.random.Random

suspend fun WebSocketConnection.sendText(text: String) {
  sendBytes(text.encodeToByteArray())
}

suspend fun WebSocketConnection.readText() =
  readBinary().decodeToString()

suspend fun WebSocketConnection.echoText(text: String) {
  sendText(text)
  readText() shouldEquals text
}

suspend fun WebSocketConnection.sendBytes(data: ByteArray) {
  write(MessageType.BINARY).useAsync { msg ->
    data.wrap {
      msg.writeFully(it)
    }
  }
}

suspend fun WebSocketConnection.readBinary() = read().useAsync {
  it.readBytes()
}

suspend fun WebSocketConnection.echoBytes(data: ByteArray) {
  sendBytes(data)
  readBinary() shouldContentEquals data
}

suspend fun WebSocketConnection.echoBytes(dataSize: Int) {
  echoBytes(Random.nextBytes(dataSize))
}

suspend fun ws(func: suspend (WebSocketConnection) -> Unit) {
  MultiFixedSizeThreadNetworkDispatcher(4).use { nd ->
    HttpClient.create(nd).use { client ->
      client.connectWebSocket(
        uri = HTTP_WS_URL,
      ).start().useAsync { ws ->
        func(ws.connection)
      }
    }
  }
}
