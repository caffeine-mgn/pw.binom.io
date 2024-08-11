package pw.binom.wasm

object MemorySectionReader {
  fun read(input: InputReader) {
    input.readVec {
      input.readLimit(min = {}, range = { min, max -> })
    }
  }
}
