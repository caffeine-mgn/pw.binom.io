package pw.binom.wasm.visitors

import pw.binom.io.Input
import pw.binom.wasm.MemoryId

interface DataSectionVisitor {
  companion object {
    val SKIP = object : DataSectionVisitor {}
  }

  fun active(memoryId: MemoryId): ExpressionsVisitor = ExpressionsVisitor.SKIP
  fun active(): ExpressionsVisitor = ExpressionsVisitor.SKIP
  fun passive() {}
  fun data(input: Input) {
    input.skipAll()
  }

  fun elementStart() {}
  fun elementEnd() {}
  fun start() {}
  fun end() {}
}
