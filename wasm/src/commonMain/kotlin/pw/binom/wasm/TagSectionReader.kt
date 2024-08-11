package pw.binom.wasm

object TagSectionReader {
  fun read(input: InputReader) {
    input.readVec {
      check(input.readUByte() == 0u.toUByte())
      input.readVarUInt32L()
    }
  }
}
