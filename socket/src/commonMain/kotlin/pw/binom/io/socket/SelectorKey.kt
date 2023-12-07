package pw.binom.io.socket

import pw.binom.io.Closeable

expect class SelectorKey : Closeable {
  var attachment: Any?
  val listenFlags: Int
  val readFlags: ListenFlags
  val isClosed: Boolean
  val selector: Selector
  fun updateListenFlags(listenFlags: Int): Boolean
}

fun SelectorKey.addListen(code: Int) = updateListenFlags(listenFlags or code)

fun SelectorKey.removeListen(code: Int) = updateListenFlags((listenFlags.inv() or code).inv())
