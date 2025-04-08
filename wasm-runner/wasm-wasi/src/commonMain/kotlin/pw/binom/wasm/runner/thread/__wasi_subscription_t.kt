package pw.binom.wasm.runner.thread

import pw.binom.wasm.runner.Pointer
import kotlin.jvm.JvmInline

/**
 * Subscription to an event.
 */
@JvmInline
value class __wasi_subscription_t(val pointer: Pointer) {
  companion object {
    const val SIZE_IN_BYTES = 48
    const val ALIGN = 8
  }

  /**
   * User-provided value that is attached to the subscription in the
   * implementation and returned through `event::userdata`.
   */
  var userdata: __wasi_userdata_t
    get() = pointer.loadULong()
    set(value) {
      pointer.storeULong(value)
    }

  /**
   * The type of the event to which to subscribe, and its contents
   */
  val u: __wasi_subscription_u_t
    get() = __wasi_subscription_u_t(pointer.withOffset(8u))
}
