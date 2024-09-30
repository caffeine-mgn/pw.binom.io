package pw.binom.io.httpServer

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import pw.binom.ByteBufferPool
import pw.binom.InternalLog
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.bufferedAsciiWriter
import pw.binom.io.http.Headers
import pw.binom.io.readBytes
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.useAsync
import pw.binom.network.TcpServerConnection
import pw.binom.network.tcpConnect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

val okHandler =
  Handler {
    it.response().useAsync {
      it.status = 202
      it.headers.contentType = "text/html;charset=utf-8"
      it.startWriteText().useAsync {
        it.append("Hello! Привет в UTF-8")
      }
    }
  }

class KeepAliveTest {

  object LOG : InternalLog {
    override fun log(level: InternalLog.Level, file: String?, line: Int?, method: String?, text: () -> String) {
      val text = text()
      try {
        print("$level $text ($file:$line)")
        if (file?.startsWith("SelectorKey") == true) {
          print(Throwable().stackTraceToString().split('\n').joinToString("->"))
        }
        println()
      } catch (e: Throwable) {
        throw e
      }
    }
  }

  @Test
  fun test() = runTest {
    network { nd ->
//      InternalLog.internalDefault.setValue(LOG)
      val server = HttpServer2(
        handler = HttpHandler { ex ->
          ex.input.readBytes()
          ex.startResponse(200, Headers.CONTENT_LENGTH to "0")
        },
        dispatcher = nd,
        byteBufferPool = ByteBufferPool(10)
      )
      val port = TcpServerConnection.randomPort()
      server.listen(InetSocketAddress.resolve("127.0.0.1", port))
      server.useAsync {
        nd.tcpConnect(InetSocketAddress.resolve("127.0.0.1", port)).useAsync { client ->
          client.bufferedAsciiWriter(closeParent = false).useAsync { writer ->
            client.bufferedAsciiReader(closeParent = false).useAsync { reader ->
              writer.append("GET / HTTP/1.1\r\n")
                .append("Host: 127.0.0.1\r\n")
                .append("Content-Length: 0\r\n")
                .append("Connection: ${Headers.KEEP_ALIVE}\r\n")
                .append("\r\n")
              writer.flush()

              assertEquals("HTTP/1.1 200 OK", reader.readln())
              assertEquals("content-length: 0", reader.readln())
              assertEquals("Connection: keep-alive", reader.readln())
              assertEquals("", reader.readln())

              delay(1.seconds)
              writer.append("GET / HTTP/1.1\r\n")
                .append("Host: 127.0.0.1\r\n")
                .append("Content-Length: 0\r\n")
                .append("Connection: ${Headers.KEEP_ALIVE}\r\n")
                .append("\r\n")
              writer.flush()

              assertEquals("HTTP/1.1 200 OK", reader.readln())
              assertEquals("content-length: 0", reader.readln())
              assertEquals("Connection: keep-alive", reader.readln())
              assertEquals("", reader.readln())
            }
          }
        }
      }
    }
  }
}
