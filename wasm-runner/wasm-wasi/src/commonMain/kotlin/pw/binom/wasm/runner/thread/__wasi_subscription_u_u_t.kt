package pw.binom.wasm.runner.thread

import pw.binom.wasm.runner.Pointer
import kotlin.jvm.JvmInline

/**
 * The contents of a `subscription`.
 */
@JvmInline
value class __wasi_subscription_u_u_t(val pointer: Pointer) {
  companion object {
    const val SIZE_IN_BYTES = 32
  }

  val clock: __wasi_subscription_clock_t
    get() = __wasi_subscription_clock_t(pointer)
  val fd_read: __wasi_subscription_fd_readwrite_t
    get() = __wasi_subscription_fd_readwrite_t(pointer)
  val fd_write: __wasi_subscription_fd_readwrite_t
    get() = __wasi_subscription_fd_readwrite_t(pointer)
}
