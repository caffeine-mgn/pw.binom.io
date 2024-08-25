package pw.binom.wasm.readers

import pw.binom.wasm.WasmInput
import pw.binom.wasm.readVec

object TagSectionReader {
  fun read(input: WasmInput) {
    input.readVec {
      check(input.i8u() == 0u.toUByte())
      input.v32u()
    }
  }
}
