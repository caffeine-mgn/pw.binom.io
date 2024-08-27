package pw.binom.wasm.visitors

import pw.binom.wasm.TypeId

interface TagSectionVisitor {
  companion object {
    val SKIP = object : TagSectionVisitor {}
  }

  fun start() {}
  fun tag(type: TypeId) {}
  fun end() {}
}
