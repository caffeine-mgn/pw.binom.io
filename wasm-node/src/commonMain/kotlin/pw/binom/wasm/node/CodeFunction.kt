package pw.binom.wasm.node

import pw.binom.wasm.visitors.CodeSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ValueVisitor
import kotlin.js.JsName

class CodeFunction : CodeSectionVisitor.CodeVisitor {
  val locals = ArrayList<Local>()
  @JsName("codeF")
  var code = Expressions()
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
