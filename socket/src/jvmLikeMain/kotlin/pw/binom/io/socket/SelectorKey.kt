package pw.binom.io.socket

import com.jakewharton.cite.__LINE__
import pw.binom.InternalLog
import pw.binom.io.Closeable
import java.nio.channels.SelectionKey

actual class SelectorKey(val native: SelectionKey, actual val selector: Selector) : Closeable {
  private val logger = InternalLog.file("SelectorKey_${System.identityHashCode(native.selector())}")

  override fun close() {
    logger.info(line = __LINE__) { "Closing" }
    closed = true
    native.cancel()
  }

  actual var attachment: Any? = null
  internal var isErrorHappened = false
  actual val readFlags: ListenFlags
    get() {
      if (closed) {
        return ListenFlags.ZERO
      }
      var r = ListenFlags.ZERO
      if (isErrorHappened) {
        r = r.withRead.withError
      }
      if (native.isAcceptable || native.isReadable || native.isConnectable) {
        r = r.withRead
      }
      if (native.isWritable || native.isConnectable) {
        r = r.withWrite
      }
      return r
    }

  private var closed = false

  internal fun clearNativeListenFlags() {
    internalListenFlags = ListenFlags.ZERO
    try {
      native.interestOps(0)
      logger.info(line = __LINE__) { "Cleared listener flags" }
    } catch (e: java.nio.channels.CancelledKeyException) {
      logger.info(line = __LINE__) { "Can't clear listener flags" }
      closed = true
    }
  }

  actual fun updateListenFlags(listenFlags: ListenFlags): Boolean {
    if (closed) {
      logger.info(line = __LINE__) { "Can't update flags to ${commonFlagsToString(listenFlags)}: socket closed" }
      return false
    }
    val old = internalListenFlags
    internalListenFlags = listenFlags

    var r = 0
    if (ListenFlags.ERROR in listenFlags || ListenFlags.READ in listenFlags) {
      r = r or SelectionKey.OP_READ or SelectionKey.OP_ACCEPT
    }

    if (ListenFlags.WRITE in listenFlags) {
      r = r or SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT
    }
    return try {
      native.interestOps(r and native.channel().validOps())
      logger.info(line = __LINE__) {
        "Update flags ${commonFlagsToString(old)}->${commonFlagsToString(listenFlags)}: ${Throwable().stackTraceToString()}"
      }
      true
    } catch (e: java.nio.channels.CancelledKeyException) {
      logger.info(line = __LINE__) {
        "Can't update flags ${commonFlagsToString(old)}->${commonFlagsToString(listenFlags)}: Locks like socket closed. Set state to close"
      }
      closed = true
      false
    }
  }

  private var internalListenFlags = ListenFlags.ZERO

  actual val listenFlags: ListenFlags
    get() = internalListenFlags
  actual val isClosed: Boolean
    get() =
      when {
        closed -> true
        else -> {
          if (native.isValid) {
            false
          } else {
            closed = true
            true
          }
        }
      }

  override fun toString(): String = "SelectorKey(${System.identityHashCode(native.selector())})"
}
