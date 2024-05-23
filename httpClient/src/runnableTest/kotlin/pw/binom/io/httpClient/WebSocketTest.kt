package pw.binom.io.httpClient

import pw.binom.io.bufferedWriter
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.readBytes
import pw.binom.io.use
import pw.binom.io.useAsync
import pw.binom.network.MultiFixedSizeThreadNetworkDispatcher
import pw.binom.testing.Testing
import pw.binom.testing.shouldEquals
import pw.binom.url.toURL
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test

class WebSocketTest {
  @Test
  fun test() = Testing.async {
    MultiFixedSizeThreadNetworkDispatcher(4).use { nd ->
      val testText = Random.nextUuid().toString()
      HttpClient.create(nd).use { client ->
        client.connectWebSocket(
          uri = "http://127.0.0.1:7142/".toURL()
        ).start().useAsync { ws ->
          ws.write(MessageType.TEXT).bufferedWriter().useAsync { msg ->
            msg.append(testText)
          }
          ws.read().useAsync { msg ->
            val txt = msg.readBytes().decodeToString()
            txt shouldEquals testText
          }
        }
      }
    }
  }
}
