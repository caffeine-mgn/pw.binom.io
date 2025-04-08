package pw.binom.wasm.wasi

import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

/**
 * The contents of a `subscription` when type is type is
 * `eventtype::fd_read` or `eventtype::fd_write`.
 */
@OptIn(UnsafeWasmMemoryApi::class)
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
