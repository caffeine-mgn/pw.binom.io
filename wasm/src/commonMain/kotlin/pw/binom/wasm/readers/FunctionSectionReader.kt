package pw.binom.wasm.readers

import pw.binom.wasm.FunctionId
import pw.binom.wasm.Types
import pw.binom.wasm.WasmInput
import pw.binom.wasm.visitors.FunctionSectionVisitor

object FunctionSectionReader {
  fun read(input: WasmInput, visitor: FunctionSectionVisitor) {
    visitor.start()
    val index = input.v32u()
    visitor.value(FunctionId(index))
    visitor.end()
  }
}
