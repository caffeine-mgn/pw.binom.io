package pw.binom.wasm

object GlobalSectionReader {
  fun read(input: StreamReader) {
    input.readVec {
      input.readValueType()
      var mutable = input.v1u()
      ExpressionReader.readExpressions(input = input, visitor = ExpressionsVisitor.STUB)
    }
  }
}
