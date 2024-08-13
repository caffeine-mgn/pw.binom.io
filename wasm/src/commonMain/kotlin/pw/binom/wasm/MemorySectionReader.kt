package pw.binom.wasm

object MemorySectionReader {
  fun read(input: StreamReader) {
    input.readVec {
      input.readLimit(min = {}, range = { min, max -> })
    }
  }
}
