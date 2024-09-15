package pw.binom.wasm.node

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.FunctionSectionVisitor

class FunctionSection : FunctionSectionVisitor, MutableList<TypeId> by ArrayList() {

  override fun start() {
    super.start()
    clear()
  }

  override fun value(int: TypeId) {
    super.value(int)
    this += int
  }

  override fun end() {
    super.end()
  }

  fun accept(visitor: FunctionSectionVisitor) {
    visitor.start()
    forEach {
      visitor.value(it)
    }
    visitor.end()
  }
}
