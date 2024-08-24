package pw.binom.wasm.readers

import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.StreamReader
import pw.binom.wasm.readValueType
import pw.binom.wasm.visitors.GlobalSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

/**
 * https://webassembly.github.io/gc/core/binary/modules.html#binary-globalsec
 */
object GlobalSectionReader {
  fun read(input: StreamReader, visitor: GlobalSectionVisitor) {
    visitor.start()
    input.readVec {
      input.readValueType(visitor = visitor.type())
      val mutable = input.v1u()
      ExpressionReader.readExpressions(input = input, visitor = visitor.code(mutable = mutable))
    }
    visitor.end()
  }
}
