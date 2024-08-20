package pw.binom.wasm.readers

import pw.binom.io.use
import pw.binom.wasm.visitors.CodeSectionVisitor
import pw.binom.wasm.ExpressionReader
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.StreamReader
import pw.binom.wasm.readValueType
import pw.binom.wasm.visitors.ValueVisitor

/**
 * https://webassembly.github.io/exception-handling/core/binary/modules.html#binary-codesec
 * https://webassembly.github.io/exception-handling/core/binary/instructions.html#binary-instr
 * https://github.com/WebAssembly/design/blob/main/BinaryEncoding.md#function-bodies
 * https://chromium.googlesource.com/v8/v8/+/refs/heads/main/src/wasm/wasm-opcodes.h
 * https://www.w3.org/TR/wasm-core-2/
 */
object CodeSectionReader {
  fun read(input: StreamReader, visitor: CodeSectionVisitor) {
    val sizeInBytes = input.v32u()
    visitor.start(sizeInBytes)
    if (sizeInBytes == 0u) {
      visitor.end()
      return
    }
    input.withLimit(sizeInBytes).use { sectionInput ->
      sectionInput.readVec {
        val size = sectionInput.v32u()
        val type = sectionInput.readValueType(visitor = ValueVisitor.EMPTY)
        repeat(size.toInt()) {
          // TODO
//          visitor.local(type)
        }
      }
      ExpressionReader.readExpressions(input = sectionInput, visitor = ExpressionsVisitor.Companion.STUB)
    }
  }


}
