package pw.binom.io.socket

import pw.binom.io.Closeable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SelectorKey : Closeable {
  var attachment: Any?
  val listenFlags: ListenFlags
  val readFlags: ListenFlags
  val isClosed: Boolean
  val selector: Selector
  fun updateListenFlags(listenFlags: ListenFlags): Boolean
  override fun close()
}

fun SelectorKey.addListen(code: ListenFlags) = updateListenFlags(listenFlags + code)

fun SelectorKey.removeListen(code: ListenFlags) = updateListenFlags(listenFlags - code)
