package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader
import pw.binom.wasm.WasmInput
import pw.binom.wasm.readValueType
import pw.binom.wasm.readVec
import pw.binom.wasm.visitors.GlobalSectionVisitor

/**
 * https://webassembly.github.io/gc/core/binary/modules.html#binary-globalsec
 */
object GlobalSectionReader {
  fun read(input: WasmInput, visitor: GlobalSectionVisitor) {
    visitor.start()
    input as StreamReader
    println("GlobalSection position: 0x${input.globalCursor.toString(16)}")
    input.readVec { // 177
      input.readValueType(visitor = visitor.type())
      val mutable = input.v1u()
      ExpressionReader.readExpressions(input = input, visitor = visitor.code(mutable = mutable))
    }
    visitor.end()
  }
}
