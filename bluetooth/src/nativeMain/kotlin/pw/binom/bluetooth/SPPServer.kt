package pw.binom.bluetooth

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.bluetooth.*
import pw.binom.io.Closeable
import kotlin.concurrent.AtomicInt

@OptIn(ExperimentalForeignApi::class)
actual class SPPServer(val native: CPointer<NSPPServer>) : Closeable {

  private val closed = AtomicInt(0)
  actual override fun close() {
    if (!closed.compareAndSet(0, 1)) {
      return
    }
    closeSPPServer(native)
  }

  actual fun accept(): SPPConnection? {
    val ptr = acceptSPPClient(native) ?: return null
    return SPPConnection(ptr)
  }
}
