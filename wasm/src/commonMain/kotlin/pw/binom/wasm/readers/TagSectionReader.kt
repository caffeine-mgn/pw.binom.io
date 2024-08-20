package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader

object TagSectionReader {
  fun read(input: StreamReader) {
    input.readVec {
      check(input.readUByte() == 0u.toUByte())
      input.v32u()
    }
  }
}
