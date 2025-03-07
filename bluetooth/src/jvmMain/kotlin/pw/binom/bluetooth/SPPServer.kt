package pw.binom.bluetooth

import com.sun.jna.Pointer
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import java.util.concurrent.atomic.AtomicBoolean

actual class SPPServer(val native: Pointer) : Closeable {
  private val closed = AtomicBoolean(false)

  private fun ensureOpened() {
    if (!closed.get()) {
      throw ClosedException()
    }
  }

  actual fun accept(): SPPConnection? {
    ensureOpened()
    val ptr = NativeLibrary.INSTANCE.acceptSPPClient(native) ?: return null
    return SPPConnection(ptr)
  }

  actual override fun close() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    NativeLibrary.INSTANCE.closeSPPServer(native)
  }
}
