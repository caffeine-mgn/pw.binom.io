package pw.binom.wasm.readers

import pw.binom.wasm.TypeId
import pw.binom.wasm.WasmInput
import pw.binom.wasm.readVec
import pw.binom.wasm.visitors.FunctionSectionVisitor

object FunctionSectionReader {
  fun read(input: WasmInput, visitor: FunctionSectionVisitor) {
    visitor.start()
    input.readVec {
      val index = input.v32u()
      visitor.value(TypeId(index))
    }
    visitor.end()
  }
}
