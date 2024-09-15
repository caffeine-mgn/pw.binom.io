package pw.binom.wasm.node

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.TypeSectionVisitor

class TypeSection : TypeSectionVisitor, MutableList<RecType> by ArrayList() {
  override fun start() {
    clear()
  }

  operator fun get(type: TypeId): RecType = get(type.value.toInt())

  override fun recType(): TypeSectionVisitor.RecTypeVisitor {
    val e = RecType()
    this += e
    return e
  }

  override fun end() {
  }

  fun accept(visitor: TypeSectionVisitor) {
    visitor.start()
    forEach {
      it.accept(visitor.recType())
    }
    visitor.end()
  }
}
