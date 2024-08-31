package pw.binom.wasm.dom

import pw.binom.wasm.visitors.CodeSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ValueVisitor

class CodeFunction : CodeSectionVisitor.CodeVisitor {
  private val locals = ArrayList<Local>()
  private var code = Expressions()
  override fun start() {
    super.start()
    locals.clear()
  }

  override fun end() {
    super.end()
  }

  override fun local(size: UInt): ValueVisitor {
    val e = ValueType()
    locals += Local(type = e, count = size)
    return e
  }

  override fun code(): ExpressionsVisitor = code

  fun accept(visitor: CodeSectionVisitor.CodeVisitor) {
    visitor.start()
    locals.forEach {
      it.type.accept(visitor.local(size = it.count))
    }
    code.accept(visitor.code())
    visitor.end()
  }
}
