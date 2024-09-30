package pw.binom.wasm.node

import pw.binom.wasm.visitors.ElementSectionVisitor

class ElementSection : ElementSectionVisitor, MutableList<Element> by ArrayList() {

  override fun start() {
    clear()
  }

  override fun end() {
  }

  override fun type0(): ElementSectionVisitor.Type0Visitor {
    val e = Element.Type0()
    this += e
    return e
  }

  fun accept(visitor: ElementSectionVisitor) {
    visitor.start()
    forEach {
      it.accept(visitor)
    }
    visitor.end()
  }
}
