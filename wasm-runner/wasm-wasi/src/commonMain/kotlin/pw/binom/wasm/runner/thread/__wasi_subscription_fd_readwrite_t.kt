package pw.binom.wasm.runner.thread

import pw.binom.wasm.runner.Pointer
import kotlin.jvm.JvmInline

/**
 * The contents of a `subscription` when type is type is
 * `eventtype::fd_read` or `eventtype::fd_write`.
 */
@JvmInline
value class __wasi_subscription_fd_readwrite_t(val pointer: Pointer) {
  companion object {
    const val SIZE_BYTES = 4
    const val ALIGN = 4
  }

  /**
   * The file descriptor on which to wait for it to become ready for reading or writing.
   */
  var file_descriptor: __wasi_fd_t
    get() = pointer.loadInt()
    set(value) {
      pointer.storeInt(value)
    }
}
