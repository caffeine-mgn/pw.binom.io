package pw.binom.wasm.node

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.FunctionSectionVisitor

class FunctionSection : FunctionSectionVisitor {
  val elements = ArrayList<FunctionId>()

  override fun start() {
    super.start()
    elements.clear()
  }

  override fun value(int: FunctionId) {
    super.value(int)
    elements += int
  }

  override fun end() {
    super.end()
  }

  fun accept(visitor: FunctionSectionVisitor) {
    visitor.start()
    elements.forEach {
      visitor.value(it)
    }
    visitor.end()
  }
}
