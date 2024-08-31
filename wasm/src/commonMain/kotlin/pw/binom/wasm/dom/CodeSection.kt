package pw.binom.wasm.dom

import pw.binom.wasm.visitors.CodeSectionVisitor

class CodeSection : CodeSectionVisitor {

  val functions = ArrayList<CodeFunction>()

  override fun start() {
    functions.clear()
  }

  override fun code(): CodeSectionVisitor.CodeVisitor {
    val e = CodeFunction()
    functions += e
    return e
  }

  override fun end() {
    super.end()
  }

  fun accept(visitor: CodeSectionVisitor) {
    visitor.start()
    functions.forEach {
      it.accept(visitor.code())
    }
    visitor.end()
  }
}
