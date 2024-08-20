package pw.binom.wasm.readers

import pw.binom.wasm.visitors.CustomSectionVisitor
import pw.binom.wasm.StreamReader

object CustomSectionReader {
  fun read(input: StreamReader, visitor: CustomSectionVisitor) {
    val sectionName = input.readString()
    visitor.start(sectionName)
    visitor.data(input)
    visitor.end()
    input.skipOther()
  }
}
