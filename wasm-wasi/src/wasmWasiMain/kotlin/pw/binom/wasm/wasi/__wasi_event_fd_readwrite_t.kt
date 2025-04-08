package pw.binom.wasm.wasi

import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

/**
 * The contents of an `event` when type is `eventtype::fd_read`
 * or `eventtype::fd_write`.
 */
@OptIn(UnsafeWasmMemoryApi::class)
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
    get() = (pointer + 8).loadUShort()
    set(value) {
      (pointer + 8).storeUShort(value)
    }
}
