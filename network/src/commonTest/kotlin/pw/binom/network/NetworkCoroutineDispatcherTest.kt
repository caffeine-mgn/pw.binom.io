package pw.binom.network

import kotlinx.coroutines.test.runTest
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.bufferedAsciiWriter
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.use
import pw.binom.io.useAsync
import kotlin.test.Test

class NetworkCoroutineDispatcherTest {
  @Test
  fun clientTest() =
    runTest(dispatchTimeoutMs = 10_000) {
      val nd = NetworkCoroutineDispatcherImpl()
      println("Dispatcher: ${getDispatcher()}")
      val client = nd.tcpConnect(InetSocketAddress.resolve("ya.ru", 443))
//            val client = nd.tcpConnect(NetworkAddress.Immutable("127.0.0.1",4444))
      println("Connected!")
      client.bufferedAsciiWriter(closeParent = false).useAsync {
        it.append("GET / HTTP/1.1\r\n")
          .append("Host: ya.ru\r\n")
          .append("\r\n")
        it.flush()
      }
      println("Getting data...")
      val text =
        client.bufferedAsciiReader(closeParent = false).useAsync {
          it.readln()
        }
      println("txt: $text")
      println("Dispatcher: ${getDispatcher()}")
    }
}
