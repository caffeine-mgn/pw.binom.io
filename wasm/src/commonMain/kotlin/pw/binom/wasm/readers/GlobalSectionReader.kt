package pw.binom.wasm.readers

import pw.binom.wasm.ExpressionReader
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.StreamReader
import pw.binom.wasm.readValueType
import pw.binom.wasm.visitors.ValueVisitor

object GlobalSectionReader {
  fun read(input: StreamReader) {
    input.readVec {
      input.readValueType(visitor = ValueVisitor.EMPTY)
      input.v1u()
      ExpressionReader.readExpressions(input = input, visitor = ExpressionsVisitor.Companion.STUB)
    }
  }
}
