package pw.binom.wasm

object TagSectionReader {
  fun read(input: StreamReader) {
    input.readVec {
      check(input.readUByte() == 0u.toUByte())
      input.v32u()
    }
  }
}
