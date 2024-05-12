package pw.binom.io.socket

import pw.binom.io.Closeable

expect class SelectorKey : Closeable {
  var attachment: Any?
  val listenFlags: ListenFlags
  val readFlags: ListenFlags
  val isClosed: Boolean
  val selector: Selector
  fun updateListenFlags(listenFlags: ListenFlags): Boolean
}

fun SelectorKey.addListen(code: ListenFlags) = updateListenFlags(listenFlags + code)

fun SelectorKey.removeListen(code: ListenFlags) = updateListenFlags(listenFlags - code)
