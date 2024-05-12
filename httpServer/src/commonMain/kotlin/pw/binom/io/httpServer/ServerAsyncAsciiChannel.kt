package pw.binom.io.httpServer

import pw.binom.ByteBufferPool
import pw.binom.date.DateTime
import pw.binom.io.AsyncChannel
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.socket.InetAddress
import pw.binom.io.socket.InetSocketAddress

class ServerAsyncAsciiChannel(pool: ByteBufferPool, channel: AsyncChannel, val address: InetAddress) :
  AsyncAsciiChannel(channel = channel, pool = pool) {
  var lastActive = DateTime.nowTime
    private set

  fun activeUpdate() {
    lastActive = DateTime.nowTime
  }
}
