package pw.binom.wasm.dom

import pw.binom.wasm.visitors.TableVisitor
import pw.binom.wasm.visitors.ValueVisitor
import kotlin.js.JsName

class Table(
  @JsName("typeF") var type: RefType,
  var min: UInt,
  var max: UInt?,
) : TableVisitor {
  override fun start() {
    super.start()
  }

  override fun range(min: UInt, max: UInt) {
    this.min = min
    this.max = max
    super.range(min, max)
  }

  override fun range(min: UInt) {
    this.min = min
    this.max = null
    super.range(min)
  }

  override fun type(): ValueVisitor.RefVisitor {
    val e = RefType()
    this.type = e
    return e
  }

  override fun end() {
    super.end()
  }

  fun accept(visitor: TableVisitor) {
    visitor.start()
    visitor.end()
  }
}
