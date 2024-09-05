package pw.binom.wasm.node

import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor
import kotlin.js.JsName

class ImportGlobal(override var module: String, override var field: String) : ImportSectionVisitor.GlobalVisitor,
    Import {
  @JsName("typeF")
  var type = ValueType()
  var mutable = false

  override fun type(): ValueVisitor = type

  override fun mutable(value: Boolean) {
    this.mutable = value
  }

  fun accept(visitor: ImportSectionVisitor.GlobalVisitor) {
    visitor.start()
    type.accept(visitor.type())
    visitor.mutable(mutable)
    visitor.end()
  }

  override fun accept(visitor: ImportSectionVisitor) {
    accept(
      visitor.global(
        module = module,
        field = field
      )
    )
  }
}
