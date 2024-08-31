package pw.binom.wasm.dom

import pw.binom.wasm.visitors.TypeSectionVisitor

class TypeSection : TypeSectionVisitor {
  val types = ArrayList<RecType>()
  override fun start() {
    types.clear()
  }

  override fun recType(): TypeSectionVisitor.RecTypeVisitor {
    val e = RecType()
    types += e
    return e
  }

  override fun end() {
  }

  fun accept(visitor: TypeSectionVisitor) {
    visitor.start()
    types.forEach {
      it.accept(visitor.recType())
    }
    visitor.end()
  }
}
