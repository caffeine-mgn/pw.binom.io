package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader

object DataCountSectionReader {
  fun read(input: StreamReader) {
    input.v32u()
  }
}
