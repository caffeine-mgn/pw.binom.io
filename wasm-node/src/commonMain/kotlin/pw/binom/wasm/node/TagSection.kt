package pw.binom.wasm.node

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.TagSectionVisitor

class TagSection : TagSectionVisitor,MutableList<TypeId> by ArrayList() {
  override fun start() {
    clear()
  }

  override fun tag(type: TypeId) {
    this += type
  }

  override fun end() {
    super.end()
  }

  fun accept(visitor: TagSectionVisitor) {
    visitor.start()
    forEach {
      visitor.tag(it)
    }
    visitor.end()
  }
}
