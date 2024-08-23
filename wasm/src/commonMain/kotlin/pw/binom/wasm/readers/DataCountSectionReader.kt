package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader
import pw.binom.wasm.visitors.DataCountSectionVisitor

object DataCountSectionReader {
  fun read(input: StreamReader, visitor: DataCountSectionVisitor) {
    visitor.start()
    visitor.value(input.v32u())
    visitor.end()
  }
}
