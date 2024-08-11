package pw.binom.wasm

object FunctionSectionReader {
  fun read(input: InputReader, visitor: FunctionSectionVisitor) {
    visitor.start()
    val index = input.readVarUInt32AsInt()
    visitor.value(index)
    visitor.end()
  }
}
