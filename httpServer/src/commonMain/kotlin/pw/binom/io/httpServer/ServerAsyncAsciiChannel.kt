package pw.binom.io.httpServer

import pw.binom.date.Date
import pw.binom.io.AsyncChannel
import pw.binom.io.ByteBuffer
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.pool.ObjectPool

class ServerAsyncAsciiChannel(pool: ObjectPool<ByteBuffer>, channel: AsyncChannel) :
    AsyncAsciiChannel(channel = channel, pool = pool) {
    var lastActive = Date.nowTime
        private set

    fun activeUpdate() {
        lastActive = Date.nowTime
    }
}
