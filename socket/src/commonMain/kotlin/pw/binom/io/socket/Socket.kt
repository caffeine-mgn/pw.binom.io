package pw.binom.io.socket

import pw.binom.io.Closeable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface Socket : Closeable {
  var blocking: Boolean
  val tcpNoDelay: Boolean
  val id: String

  fun setTcpNoDelay(value: Boolean): Boolean

  companion object;
}
