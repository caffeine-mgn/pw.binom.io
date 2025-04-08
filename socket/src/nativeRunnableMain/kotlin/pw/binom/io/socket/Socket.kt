package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.socket.NSocket
import pw.binom.io.Closeable
import pw.binom.io.InHeap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual interface Socket : Closeable {
  @OptIn(ExperimentalForeignApi::class)
  val data:InHeap<NSocket>
  actual var blocking: Boolean
  val native: Int
  val server: Boolean
  var keyHash: Int
  actual val id: String


  actual companion object;

  actual val tcpNoDelay: Boolean

  actual fun setTcpNoDelay(value: Boolean): Boolean
}
