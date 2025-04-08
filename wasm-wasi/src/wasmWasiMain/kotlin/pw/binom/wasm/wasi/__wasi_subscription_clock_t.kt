package pw.binom.wasm.wasi

import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

/**
 * The contents of a `subscription` when type is `eventtype::clock`.
 */
@OptIn(UnsafeWasmMemoryApi::class)
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
    get() = (pointer + 8).loadULong()
    set(value) {
      (pointer + 8).storeULong(value)
    }

  /**
   * The amount of time that the implementation may wait
   * additionally to coalesce with other events.
   */
  var precision: __wasi_timestamp_t
    get() = (pointer + 16).loadULong()
    set(value) {
      (pointer + 16).storeULong(value)
    }

  /**
   * Flags specifying whether the timeout is absolute or relative
   */
  var flags: __wasi_subclockflags_t
    get() = (pointer + 24).loadUShort()
    set(value) {
      (pointer + 24).storeUShort(value)
    }
}
