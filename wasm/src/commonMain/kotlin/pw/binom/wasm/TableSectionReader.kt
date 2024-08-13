package pw.binom.wasm

object TableSectionReader {
  fun read(input: StreamReader) {
    input.readVec {
      input.readRefType()
      input.readLimit(min = { min -> }, range = { min, max -> })
    }
  }
}
