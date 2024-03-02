package pw.binom.mq

import pw.binom.io.AsyncCloseable

interface Consumer : AsyncCloseable {
  companion object;

  suspend fun start()

  suspend fun stop()

  suspend fun deleteAndClose()

  val isReceiving: Boolean
}
