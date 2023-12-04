package pw.binom.io.httpServer

import pw.binom.ByteBufferPool
import pw.binom.date.DateTime
import pw.binom.io.AsyncChannel
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.socket.InetNetworkAddress

class ServerAsyncAsciiChannel(pool: ByteBufferPool, channel: AsyncChannel, val address: InetNetworkAddress) :
  AsyncAsciiChannel(channel = channel, pool = pool) {
  var lastActive = DateTime.nowTime
    private set

  fun activeUpdate() {
    lastActive = DateTime.nowTime
  }
}
