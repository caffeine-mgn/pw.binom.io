package pw.binom.mq.nats.client

import pw.binom.io.AsyncChannel
import pw.binom.io.ByteBuffer
import pw.binom.io.holdState

class LoggingAsyncChannel(val source: AsyncChannel) : AsyncChannel {
  override suspend fun asyncClose() {
    source.asyncClose()
  }

  override suspend fun write(data: ByteBuffer): Int {
    val p = data.position
    val w = source.write(data)
    if (w > 0) {
      data.holdState {
        it.reset(p, w)
        println("WRITE $p->$w: ${it.toByteArray().decodeToString()}")
      }
    }
    return w
  }

  override suspend fun flush() {
    source.flush()
  }

  override val available: Int
    get() = source.available

  override suspend fun read(dest: ByteBuffer): Int {
    val p = dest.position
    val r = source.read(dest)
    dest.holdState {
      it.reset(p, r)
      println("READ ${it.toByteArray().decodeToString()}")
    }
    return r
  }
}
