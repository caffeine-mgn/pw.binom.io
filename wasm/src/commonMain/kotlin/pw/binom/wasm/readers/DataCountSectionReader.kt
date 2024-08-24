package pw.binom.wasm.readers

import pw.binom.wasm.WasmInput
import pw.binom.wasm.visitors.DataCountSectionVisitor

object DataCountSectionReader {
  fun read(input: WasmInput, visitor: DataCountSectionVisitor) {
    visitor.start()
    visitor.value(input.v32u())
    visitor.end()
  }
}
