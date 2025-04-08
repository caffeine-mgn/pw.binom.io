package pw.binom.wasm.runner.thread

import pw.binom.wasm.runner.Pointer
import kotlin.jvm.JvmInline

/**
 * The contents of a `subscription` when type is `eventtype::clock`.
 */
@JvmInline
value class __wasi_subscription_clock_t(val pointer: Pointer) {
  companion object {
    const val SIZE_IN_BYTES = 32
    const val ALIGN = 8
  }

  /**
   * The clock against which to compare the timestamp.
   */
  var id: __wasi_clockid_t
    get() = pointer.loadUInt()
    set(value) {
      pointer.storeUInt(value)
    }

  /***
   * The absolute or relative timestamp.
   */
  var timeout: __wasi_timestamp_t
    get() = pointer.loadULong(shift = 8u)
    set(value) {
      pointer.storeULong(value, shift = 8u)
    }

  /**
   * The amount of time that the implementation may wait
   * additionally to coalesce with other events.
   */
  var precision: __wasi_timestamp_t
    get() = pointer.loadULong(shift = 16u)
    set(value) {
      pointer.storeULong(value, shift = 16u)
    }

  /**
   * Flags specifying whether the timeout is absolute or relative
   */
  var flags: __wasi_subclockflags_t
    get() = pointer.loadUShort(shift = 24u)
    set(value) {
      pointer.storeUShort(value, shift = 24u)
    }
}
