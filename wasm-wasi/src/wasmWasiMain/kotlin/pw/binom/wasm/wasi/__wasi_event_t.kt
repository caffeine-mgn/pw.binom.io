package pw.binom.wasm.wasi

import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

/**
 * An event that occurred.
 */
@OptIn(UnsafeWasmMemoryApi::class)
value class __wasi_event_t(val pointer: Pointer) {
  companion object {
    const val SIZE_IN_BYTES = 32
    const val ALIGN = 8
  }

  /**
   * User-provided value that got attached to `subscription::userdata`.
   */
  var userdata: __wasi_userdata_t
    get() = pointer.loadULong()
    set(value) {
      pointer.storeULong(value)
    }

  /**
   * If non-zero, an error that occurred while processing the subscription request.
   */
  var error: __wasi_errno_t
    get() = (pointer + 8).loadUShort()
    set(value) {
      (pointer + 8).storeUShort(value)
    }

  /**
   * The type of event that occured
   */
  var type: __wasi_eventtype_t
    get() = (pointer + 10).loadUByte()
    set(value) {
      (pointer + 10).storeUByte(value)
    }

  /**
   * The contents of the event, if it is an `eventtype::fd_read` or
   * `eventtype::fd_write`. `eventtype::clock` events ignore this field.
   */
  val fd_readwrite: __wasi_event_fd_readwrite_t
    get() = __wasi_event_fd_readwrite_t(pointer + 16)
}
