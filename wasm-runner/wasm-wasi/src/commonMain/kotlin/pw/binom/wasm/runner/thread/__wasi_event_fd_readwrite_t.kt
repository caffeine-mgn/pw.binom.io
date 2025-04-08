package pw.binom.wasm.runner.thread

import pw.binom.wasm.runner.Pointer
import kotlin.jvm.JvmInline

/**
 * The contents of an `event` when type is `eventtype::fd_read`
 * or `eventtype::fd_write`.
 */
@JvmInline
value class __wasi_event_fd_readwrite_t(val pointer: Pointer) {
  companion object {
    const val SIZE_IN_BYTES = 16
    const val ALIGN = 8
  }

  /**
   * The number of bytes available for reading or writing.
   */
  var nbytes: __wasi_filesize_t
    get() = pointer.loadULong()
    set(value) {
      pointer.storeULong(value)
    }

  /**
   * The state of the file descriptor.
   */
  var flags: __wasi_eventrwflags_t
    get() = pointer.loadUShort(shift = 8u)
    set(value) {
      pointer.storeUShort(value = value, shift = 8u)
    }
}
