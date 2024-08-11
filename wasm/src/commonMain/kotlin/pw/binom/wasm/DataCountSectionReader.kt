package pw.binom.wasm

object DataCountSectionReader {
  fun read(input: InputReader) {
    input.readVarUInt32L()
  }
}
