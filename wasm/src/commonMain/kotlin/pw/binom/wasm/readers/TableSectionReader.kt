package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader
import pw.binom.wasm.readRefType

object TableSectionReader {
  fun read(input: StreamReader) {
    input.readVec {
      input.readRefType()
      input.readLimit(min = { min -> }, range = { min, max -> })
    }
  }
}
