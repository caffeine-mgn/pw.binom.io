package pw.binom.wasm

object DataCountSectionReader {
  fun read(input: StreamReader) {
    input.v32u()
  }
}
