package pw.binom.wasm

object GlobalSectionReader {
  fun read(input: InputReader) {
    input.readVec {
      input.readValueType()
      var mutable = input.readVarUInt1()
      CodeSectionReader.readExpressions(input = input, visitor = ExpressionsVisitor.STUB)
    }
  }
}
