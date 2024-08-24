package pw.binom.wasm.readers

import pw.binom.wasm.visitors.CustomSectionVisitor
import pw.binom.wasm.WasmInput

object CustomSectionReader {
  fun read(input: WasmInput, visitor: CustomSectionVisitor) {
    val sectionName = input.string()
    visitor.start(sectionName)
    visitor.data(input)
    visitor.end()
    input.skipOther()
  }
}
