package pw.binom.wasm.readers

import pw.binom.wasm.*
import pw.binom.wasm.visitors.TableSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

object TableSectionReader {
  fun read(input: WasmInput, visitor: TableSectionVisitor) {
    visitor.start()
    input.readVec {
      input.readRefType(visitor = visitor.type())
      input.readLimit(
        min = { min -> visitor.limit(min) },
        range = { min, max -> visitor.limit(inital = min, max = max) })
    }
    visitor.end()
  }
}
