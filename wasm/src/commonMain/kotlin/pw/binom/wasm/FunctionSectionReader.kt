package pw.binom.wasm

object FunctionSectionReader {
  fun read(input: StreamReader, visitor: FunctionSectionVisitor) {
    visitor.start()
    val index = input.v32u()
    visitor.value(index)
    visitor.end()
  }
}
