package pw.binom.io.socket

import pw.binom.InternalLog
import pw.binom.io.Closeable
import java.nio.channels.SelectionKey
import kotlin.math.absoluteValue

actual class SelectorKey(val native: SelectionKey, actual val selector: Selector) : Closeable {
  private val logger = InternalLog.file("SelectorKey")

  actual override fun close() {
    logger.info(method = "close") { "Closing" }
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
      logger.info(method = "clearNativeListenFlags") { "Cleared listener flags ${native.channel()::class.java.name}@${native.channel().hashCode()}" }
    } catch (e: java.nio.channels.CancelledKeyException) {
      logger.info(method = "clearNativeListenFlags") { "Can't clear listener flags ${native.channel()::class.java.name}@${native.channel().hashCode()}" }
      closed = true
    }
  }

  actual fun updateListenFlags(listenFlags: ListenFlags): Boolean {
    if (closed) {
      logger.info(method = "updateListenFlags") { "Can't update flags to ${commonFlagsToString(listenFlags)}: socket closed" }
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
      val result = r and native.channel().validOps()
      native.interestOps(result)
      logger.info(method = "updateListenFlags") {
        "Update flags ${commonFlagsToString(old)}->${commonFlagsToString(listenFlags)} = ${javaFlagsToString(result)}"
      }
      true
    } catch (e: java.nio.channels.CancelledKeyException) {
      logger.info(method = "updateListenFlags") {
        "Can't update flags ${commonFlagsToString(old)}->${commonFlagsToString(listenFlags)}: Locks like socket closed. Set state to close"
      }
      closed = true
      false
    }
  }

  private fun javaFlagsToString(flags: Int): String {
    val result = StringBuilder()
    if (flags and SelectionKey.OP_READ != 0) {
      result.append(" OP_READ")
    }
    if (flags and SelectionKey.OP_WRITE != 0) {
      result.append(" OP_WRITE")
    }
    if (flags and SelectionKey.OP_CONNECT != 0) {
      result.append(" OP_CONNECT")
    }
    if (flags and SelectionKey.OP_ACCEPT != 0) {
      result.append(" OP_ACCEPT")
    }
    return result.toString()
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
