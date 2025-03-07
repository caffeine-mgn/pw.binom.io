package pw.binom.bluetooth

import pw.binom.io.Closeable

expect class SPPServer : Closeable {
  fun accept(): SPPConnection?
  override fun close()
}
