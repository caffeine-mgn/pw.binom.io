package pw.binom.wasm.node

import pw.binom.wasm.visitors.CodeSectionVisitor

class CodeSection : CodeSectionVisitor, MutableList<CodeFunction> by ArrayList() {

  override fun start() {
    clear()
  }

  override fun code(): CodeSectionVisitor.CodeVisitor {
    val e = CodeFunction()
    this += e
    return e
  }

  override fun end() {
    super.end()
  }

  fun accept(visitor: CodeSectionVisitor) {
    visitor.start()
    forEach {
      it.accept(visitor.code())
    }
    visitor.end()
  }
}
