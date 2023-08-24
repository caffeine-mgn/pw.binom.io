package pw.binom.io.socket

import pw.binom.io.Closeable
import java.nio.channels.SelectionKey

actual class SelectorKey(val native: SelectionKey, actual val selector: Selector) : Closeable {
  override fun close() {
    closed = true
    native.cancel()
  }

  actual var attachment: Any? = null
  internal var isErrorHappened = false
  actual val readFlags: Int
    get() {
      if (closed) {
        return 0
      }
      var r = 0
      if (isErrorHappened) {
        r = r or KeyListenFlags.READ or KeyListenFlags.ERROR
      }
      if (native.isAcceptable || native.isReadable || native.isConnectable) {
        r = r or KeyListenFlags.READ
      }
      if (native.isWritable || native.isConnectable) {
        r = r or KeyListenFlags.WRITE
      }
      return r
    }

  private var closed = false

  internal fun clearNativeListenFlags() {
    internalListenFlags = 0
    try {
      native.interestOps(0)
    } catch (e: java.nio.channels.CancelledKeyException) {
      closed = true
    }
  }

  actual fun updateListenFlags(listenFlags: Int): Boolean {
    if (closed) {
      return false
    }
    internalListenFlags = listenFlags

    var r = 0
    if (listenFlags and KeyListenFlags.ERROR != 0 || listenFlags and KeyListenFlags.READ != 0) {
      r = r or SelectionKey.OP_READ or SelectionKey.OP_ACCEPT
    }

    if (listenFlags and KeyListenFlags.WRITE != 0) {
      r = r or SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT
    }
    return try {
      native.interestOps(r and native.channel().validOps())
      true
    } catch (e: java.nio.channels.CancelledKeyException) {
      closed = true
      false
    }
  }

  private var internalListenFlags = 0

  actual val listenFlags: Int
    get() = internalListenFlags
  actual val isClosed: Boolean
    get() = closed || !native.isValid

  override fun toString(): String = buildToString()
}
