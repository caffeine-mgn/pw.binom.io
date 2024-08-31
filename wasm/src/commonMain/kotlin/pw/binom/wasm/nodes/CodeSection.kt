package pw.binom.wasm.nodes

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
}
