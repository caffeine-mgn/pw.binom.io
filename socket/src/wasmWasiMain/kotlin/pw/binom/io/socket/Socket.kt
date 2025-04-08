package pw.binom.io.socket

import pw.binom.io.Closeable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual interface Socket : Closeable {
  actual var blocking: Boolean
  val native: Int
  val server: Boolean
  var keyHash: Int
  actual val id: String


  actual companion object;

  actual val tcpNoDelay: Boolean

  actual fun setTcpNoDelay(value: Boolean): Boolean
}
