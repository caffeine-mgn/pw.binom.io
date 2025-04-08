package pw.binom.wasm.wasi

import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

@OptIn(UnsafeWasmMemoryApi::class)
value class tcp_create_socket_error_code_t(val pointer: Pointer) {
  companion object {
    const val SIZE_IN_BYTES = 8
    const val ALIGN = 4
  }

  var error
    get() = pointer.loadInt()
    set(value) {
      pointer.storeInt(value)
    }
  var socket
    get() = (pointer + 4).loadInt()
    set(value) {
      (pointer + 4).storeInt(value)
    }
}
