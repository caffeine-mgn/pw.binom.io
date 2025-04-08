package pw.binom.wasm.runner.thread

import pw.binom.wasm.runner.Pointer
import kotlin.jvm.JvmInline

@JvmInline
value class __wasi_subscription_u_t(val pointer: Pointer) {
  companion object {
    const val SIZE_BYTES = 40
    const val ALIGN = 8
  }

  var tag: UByte
    get() = pointer.loadUByte()
    set(value) {
      pointer.storeUByte(value)
    }
  val u: __wasi_subscription_u_u_t
    get() = __wasi_subscription_u_u_t(pointer.withOffset(8u))
}
