package pw.binom.wasm.nodes

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.TagSectionVisitor

class TagSection : TagSectionVisitor {
  var elements = ArrayList<TypeId>()
  override fun start() {
    elements.clear()
  }

  override fun tag(type: TypeId) {
    elements += type
  }

  override fun end() {
    super.end()
  }

  fun accept(visitor: TagSectionVisitor) {
    visitor.start()
    elements.forEach {
      visitor.tag(it)
    }
    visitor.end()
  }
}
