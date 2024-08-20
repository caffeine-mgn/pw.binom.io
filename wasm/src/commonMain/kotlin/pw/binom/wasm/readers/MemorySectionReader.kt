package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader

object MemorySectionReader {
  fun read(input: StreamReader) {
    input.readVec {
      input.readLimit(min = {}, range = { min, max -> })
    }
  }
}
