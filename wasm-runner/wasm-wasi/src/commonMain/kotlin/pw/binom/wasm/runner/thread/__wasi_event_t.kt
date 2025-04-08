package pw.binom.wasm.runner.thread

import pw.binom.wasm.runner.Pointer
import kotlin.jvm.JvmInline

/**
 * An event that occurred.
 */
@JvmInline
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
    get() = (pointer).loadUShort(shift = 8u)
    set(value) {
      (pointer).storeUShort(value, shift = 8u)
    }

  /**
   * The type of event that occured
   */
  var type: __wasi_eventtype_t
    get() = pointer.loadUByte(shift = 10u)
    set(value) {
      pointer.storeUByte(value, shift = 10u)
    }

  /**
   * The contents of the event, if it is an `eventtype::fd_read` or
   * `eventtype::fd_write`. `eventtype::clock` events ignore this field.
   */
  val fd_readwrite: __wasi_event_fd_readwrite_t
    get() = __wasi_event_fd_readwrite_t(pointer.withOffset(16u))
}
